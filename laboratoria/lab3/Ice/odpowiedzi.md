Odpowiedzi na wybrane punkty z części Zeroc ICE (2.2)

3. Dla języka Python mamy analogiczne polecenie `slice2py`, z kolei dla C++ jest `slice2cpp`.

5) W katalogu _generated_ znajdują się klasy wygenerowane poprzez polecenie z punktu 3.
Ich nazwy pokrywają się z nazwami odpowiednich struktur, interfejsów itp. z pliku _slice/calculator.ice_.
Poza tym jest interfejs i klasa go implementująca do Proxy.

6) Różnica między **checked cast** a **unchecked cast**: unchecked cast nie sprawdza type safety w czasie wykonania.
"co się dzieje "w sieci"? dlaczego "działa" tak wolno?" -> czy chodzi o symulowane "obciążenie" sieci?

7) Serwer używa gniazd _tcp_ i _udp_ na adresie 127.0.0.2 i porcie o numerze 10000. Można to sprawdzić (zweryfikować)
za pomocą polecenia `netstat` z flagami `-ano`.

9. Odpowiedzi na kolejne pytania:
- Te zmienne to **calcServant1** i **calcServant2**
- Serwer udostępnia 2 sztuki obiektów, nazywają się one _calc11_ i _calc22_
- Z tym pierwszym, tzn. _calc11_
- Z obiektem **calcServant1**
- Klient uzyskał tę referencję poprzez Proxy, następnie zawęził typy (_cast_)
- Próba zrealizowania wywołania na nieistniejącym obiekcie powoduje wyrzucenie wyjątku: `com.zeroc.Ice.ObjectNotExistException`

12) Wydaje mi się, że Wireshark _rozumie_ strukturę tych wiadomości. Żądanie zawiera informacje o obiekcie serwanta 
i metodzie, natomiast odpowiedź o powodzeniu żądania.
Aby mogła zachodzić komunikacja w scenariuszu, gdy klient i serwer są na różnych maszynach, wydaje mi się, że warto
umieścić konfigurację w oddzielnym pliku i użyć adresu IP innego niż loopback (lokalny :D) - natomiast dokładnej
wiedzy i pewności nie mam.

13) Jako że nie doszedłem do tego punktu na zajęciach (kolega obok również), to nie zrealizowałem tego punktu.

15. Oznaczenie operacji jako idempotentnej daje nam większą transparentność przy powracaniu po błędzie. 
Jako idempotentne mogą być oznaczone operacje, których wielokrotne wywołanie (z tymi samymi parametrami) da taki
sam efekt, jak jednokrotne wywołanie. Wydaje mi się, że w obecnej postaci wszystkie operacje z interfejsu **Calc**
mogą być tak oznaczone.

17) Odpowiedzi na pytania:
- Opóźnienie wynika z tego, że wywołanie jest wykonywane w oddzielnym wątku przez Proxy i stamtąd "powraca" do klienta (?)
- Warto realizować w taki sposób wywołania, które mogą zająć więcej czasu (i przez to by blokowały)
- Wydaje mi się, że nie każde (np. jeżeli drugie wywołanie korzysta z wyniku poprzedniego)

18) W pliku _config.server_ jest ustawiona pula wątków o rozmiarze 10.

19) Wywołanie _oneway_ polega na tym, że sterowanie wraca do klienta niemal natychmiast (po dostarczeniu do lokalnego
transportu). Wymogiem jest, żeby wywołanie nie zwracało wartości. W taki sposób zrealizowana może być operacja `op`, bo
ma typ zwracany **void**. Zatem po zmianie trybu Proxy na oneway, próba wywołania `add` rzuca wyjątek:
**com.zeroc.Ice.TwowayOnlyException**. W przypadku wywołania _oneway_ komunikacja pomiędzy klientem a serwerem jest
znacznie ograniczona w porównaniu do tradycyjnego wywołania _twoway_.

20) W wywołaniu _datagram_ również sterowanie wraca do klienta po dostarczeniu do lokalnego transportu, ponadto
komunikacja następuje z wykorzystaniem UDP (można użyć multicast IP). Wymogiem podobnie jest, żeby wywołanie nie 
zwracało wartości. W taki sposób zrealizowana może być operacja `op`, bo ma typ zwracany **void** - więc po zmianie 
trybu Proxy na datagram, próba wywołania `add` rzuca wyjątek: **com.zeroc.Ice.TwowayOnlyException**. 
W przypadku wywołania _datagram_ komunikacja pomiędzy klientem a serwerem jest znacznie ograniczona w porównaniu do 
tradycyjnego wywołania _twoway_, ponadto wykorzystywany jest protokół UDP, a nie TCP.

21) Zapewne tylko drugie jest skompresowane ze względu na długość wiadomości (ostatni argument wywołania). Odpowiedzi
serwera są nieskompresowane, ale mogą być skompresowane (przy obu żądaniach w Wireshark: "...sender can accept
a compressed reply").
Wydaje mi się, że aktywacja kompresji jest pożądana przy odpowiednio długich wiadomościach (wtedy istotnie kompresja
jest przydatna). Po wywołaniu z menu klienta żądania są nieskompresowane oraz w Wiresharku pojawia się:
"Compression Status: Uncompressed, sender cannot accept a compressed reply".

22) Agregacja wywołań (_batchowanie_) pozwala nam ograniczyć ruch sieciowy. Aby móc z niej korzystać, muszą to być
wywołania typu _oneway_ lub _datagram_. Zatem tak zrealizowane mogą być operacje niezwracające wartości - w naszym
przypadku jest to operacja `op`. 
Tryb pracy proxy zmieniłem na **batch oneway**. Różnica w komunikacji między klientem a serwerem polega na tym, że
dopiero po "przepchaniu" - _flush_ po stronie serwera pojawiają się "logi" komunikacji.
W Wiresharku pojawia się jako "Batch request".
