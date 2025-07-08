Odpowiedzi na wybrane punkty z części Apache Thrift (2.3)

2. Dla aplikacji w języku Python należy ten plik skompilować poleceniem:
`thrift --gen py calculator.thrift `.

4) Wydaje mi się, że utworzenie wielu serwerów dla opcji _simple_ nie działa jak w zamierzeniu, bowiem
"Zazwyczaj serwer uruchamia tylko jedną instancję obiektu implementującego interfejs (jedną usługę)"
(z materiałów do laboratorium); ale mogę się mylić. Z podobnego powodu w kodzie klienta odpowiednie
obiekty wskazują na ten sam obiekt `protocol`.

7. Liczba bajtów pola danych warstwy IV dla pojedynczego wywołania różni się w zależności od operacji - np.
dla przykładowych wywołań operacji `add` są to 33 bajty, dla operacji `op` jest to 57 bajtów.
Thrift używa protokołu transportowego **TCP**.

8. Powyższe wywołania były w trybie _Binary_. Zbadajmy analogicznie pozostałe tryby:
- dla trybu **Compact**: wywołanie `add` - 15 bajtów, wywołanie `op` - 38 bajtów
- dla trybu **JSON**: wywołanie `add` - 48 bajtów, wywołanie `op` - 71 bajtów

Porównując z zapisem komunikacji z Upela (tam tylko dla `op`) były tam odpowiednio 54 bajty, 35 bajtów
oraz 68 bajtów (w każdym przypadku o 3 mniej - ciekawe).

9. i 10. W przypadku _TMultiplexedProcessor_ wystarczy "zarejestrować" (register) _processory_ świadczące
odpowiednie usługi pod różnymi nazwami (_serviceName_). W przypadku "zwykłych" processorów nie jest to
możliwe (punkt 4.). Co do _TBinaryProtocol_ wydaje mi się, że nie na to wpływu (podobnie można zrobić
wykorzystując inne protokoły), natomiast ze względu na **warstwową** budowę Apache Thrift wybór protokołu
powinien być (poza ewentualnie pewnymi wyjątkami) niezależny od wyboru processora.
