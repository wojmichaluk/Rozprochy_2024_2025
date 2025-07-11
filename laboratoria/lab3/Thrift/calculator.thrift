
namespace cpp tutorial
namespace d tutorial
namespace dart tutorial
namespace java sr.gen.thrift
namespace php tutorial
namespace perl tutorial
namespace haxe tutorial

enum OperationType {
  SUM = 1,
  MIN = 2,
  MAX = 3,
  AVG = 4
}

struct Work {
  1: i32 num1 = 0,
  2: i32 num2,
  3: OperationType op,
  4: optional string comment,
}

/**
 * Structs can also be exceptions, if they are nasty.
 */
exception InvalidOperation {
  1: required i32 whatOp,
  2: optional string why
}

exception InvalidArguments {
  1: i32 argNo,
  2: string reason
}
 
service Calculator {
   i32 add(1:i32 num1, 2:i32 num2),
   i32 subtract(1:i32 num1, 2:i32 num2),
}
    
 
service AdvancedCalculator extends Calculator {
   double op(1:OperationType type, 2: set<double> val) throws (1: InvalidArguments ex),
   /**
        Added in 2.3.6
        Maybe it's trivial, but I like it
   **/
   string concat(1:list<string> stringList),
}
