namespace java thrift.gen
namespace py ThriftGen


/* enums */

enum BulbulatorType {
   BUL = 1,
   BULBUL = 2
}

enum DetectorType {
   SMOKE = 1,
   CARBON_MONOXIDE = 2,
   TEMPERATURE = 3
}


/* structs */

struct BlindsInfo {
   1: i32 rollPercent,
   2: string room,
}

struct BulbulatorInfo {
   1: BulbulatorType type,
   2: string name,
   3: bool isOn,
}

struct DetectorInfo {
   1: DetectorType type,
   2: map<string, i32> params,
}


/* exceptions */

exception InvalidParamName {
  1: string paramName
}

exception DangerousParamValue {
  1: string paramName,
  2: i32 dangerousValue,
  3: list<i32> bounds
}


/* services */

service Blinds {
   i32 pullUp(1: i32 percent),
   i32 pullDown(1: i32 percent),
}

service Bulbulator {
   void turnOn(),
   void turnOff(),
}

service Detector {
   map<string, i32> getParams(),
   i32 getParamValue(1: string param) throws (1: InvalidParamName ipn),
   void checkParamSafety(1: string param, 2: list<i32> bounds) throws(1: InvalidParamName ipn, 2: DangerousParamValue dpv),
}
