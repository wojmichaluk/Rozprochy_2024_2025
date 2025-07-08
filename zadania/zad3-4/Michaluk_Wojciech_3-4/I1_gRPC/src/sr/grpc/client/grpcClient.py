import grpc
from grpc_reflection.v1alpha.proto_reflection_descriptor_database import ProtoReflectionDescriptorDatabase
from google.protobuf.descriptor_pool import DescriptorPool
from google.protobuf import message_factory

# to get descriptors
def get_descriptors(desc_pool, input_type, output_type):
    request_descriptor = desc_pool.FindMessageTypeByName(input_type.full_name)
    result_descriptor = desc_pool.FindMessageTypeByName(output_type.full_name)
    return request_descriptor, result_descriptor

# trying to respect DRY rule for service2 methods
def dynamic_invocation(service_desc, method, channel, request_message, result_descriptor):
    method_full_name = f"/{service_desc.full_name}/{method.name}"
    print(f"\nDynamic invocation of method: {method_full_name}")

    stub = channel.unary_unary(
        method=method_full_name,
        request_serializer=request_message.SerializeToString,
        response_deserializer=message_factory.GetMessageClass(result_descriptor).FromString,
    )

    response = stub(request_message)
    print(f"Response received for this call:\n{response}", end='')

# based on example: https://github.com/grpc/grpc/blob/master/examples/python/helloworld/greeter_client_reflection.py
if __name__ == "__main__":
    # servers info
    host = "localhost"
    port = 50051

    with grpc.insecure_channel(f"{host}:{port}") as channel:
        reflection_db = ProtoReflectionDescriptorDatabase(channel)
        services = reflection_db.get_services()
        desc_pool = DescriptorPool(reflection_db)

        # services
        service1_desc = desc_pool.FindServiceByName(services[0])
        service2_desc = desc_pool.FindServiceByName(services[1])

        # methods
        method11, method12 = service1_desc.methods
        method21, method22 = service2_desc.methods

        # service 1, method 1
        request_descriptor, result_descriptor = get_descriptors(desc_pool, method11.input_type, method11.output_type)
        request_message = message_factory.GetMessageClass(request_descriptor)()

        # filling the field interactively
        user_input = input("Please give number from 10 to 1000: ")
        while True:
            try:
                num = int(user_input)
                if 10 <= num <= 1000:
                    request_message.num = num
                    print("Thank you!\n")
                    break
                user_input = input("Please remember about the range: ")
            except ValueError:
                user_input = input("Let it be a decimal number! ")

        # preparing for dynamic invocation
        method_full_name = f"/{service1_desc.full_name}/{method11.name}"
        print(f"Dynamic invocation of method: {method_full_name}")

        stub = channel.unary_stream(
            method=method_full_name,
            request_serializer=request_message.SerializeToString,
            response_deserializer=message_factory.GetMessageClass(result_descriptor).FromString,
        )

        response = stub(request_message)
        print("Response received for this call:")

        # saving for future client-side streaming call
        prime_numbers = []

        for num in response:
            print(num, end='')
            prime_numbers.append(num.num)
        print()

        # service 1, method 2
        request_descriptor, result_descriptor = get_descriptors(desc_pool, method12.input_type, method12.output_type)
        request_message = message_factory.GetMessageClass(request_descriptor)()

        # for the sake of streaming
        def request_stream():
            for prime in prime_numbers:
                request_message.num = prime
                yield request_message

        method_full_name = f"/{service1_desc.full_name}/{method12.name}"
        print(f"Dynamic invocation of method: {method_full_name}")

        stub = channel.stream_unary(
            method=method_full_name,
            request_serializer=lambda message: message.SerializeToString(),
            response_deserializer=message_factory.GetMessageClass(result_descriptor).FromString,
        )

        response = stub(request_stream())
        print(f"Response received for this call:\n{response}", end='')

        # service 2, method 1
        request_descriptor, result_descriptor = get_descriptors(desc_pool, method21.input_type, method21.output_type)
        request_message = message_factory.GetMessageClass(request_descriptor)()
        field1, field2 = request_message.DESCRIPTOR.fields

        field_descriptor = request_descriptor.fields_by_name["date"]
        field_message = message_factory.GetMessageClass(field_descriptor.message_type)()

        user_input = input("\nPlease enter a year from 2000 to 2025: ")
        year = None
        while True:
            try:
                year = int(user_input)
                if 2000 <= year <= 2025:
                    field_message.year = year
                    print("Thank you!\n")
                    break
                user_input = input("Please remember about the range: ")
            except ValueError:
                user_input = input("Let it be a decimal number! ")

        user_input = input("Please enter a season for this year (lowercase): ")
        season_dict = { "winter": 0, "spring": 1, "summer": 2, "fall": 3 }
        season = None
        while True:
            season = season_dict.get(user_input)
            if season is not None:
                field_message.quarter = season
                print("Thank you!\n")
                break
            user_input = input("Please enter correct name: ")

        user_input = input("Please enter salaries for this season (3 numbers, separated by single space): ")
        salaries = None
        while True:
            try:
                salaries = list(map(float, user_input.split(' ')))
                if min(salaries) >= 0:
                    print("Thank you!")
                    break
                user_input = input("Come on, salaries are positive: ")
            except ValueError:
                user_input = input("Let them be positive decimal numbers! ")

        request_message.date.CopyFrom(field_message)
        request_message.salaries.extend(salaries)

        dynamic_invocation(service2_desc, method21, channel, request_message, result_descriptor)

        # service 2, method 2
        request_descriptor, result_descriptor = get_descriptors(desc_pool, method22.input_type, method22.output_type)
        request_message = message_factory.GetMessageClass(request_descriptor)()

        request_message.year = year
        request_message.quarter = season

        dynamic_invocation(service2_desc, method22, channel, request_message, result_descriptor)
