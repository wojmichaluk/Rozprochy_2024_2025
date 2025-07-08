
#ifndef CALC_ICE
#define CALC_ICE

module Demo
{
  enum operation { MIN, MAX, AVG };

  // punkt 2.2.14
  sequence<long> seqOfNumbers;
  
  exception NoInput {};

  // punkt 2.2.14
  exception EmptySeq {};

  struct A
  {
    short a;
    long b;
    float c;
    string d;
  }

  interface Calc
  {
    long add(int a, int b);
    long subtract(int a, int b);

    // punkt 2.2.14
    float avg(seqOfNumbers s) throws EmptySeq;

    void op(A a1, short b1); //załóżmy, że to też jest operacja arytmetyczna ;)
  };

};

#endif
