Odpowiedzi na wybrane punkty z części gRPC (2.4)

7. Wykorzystywany jest protokół komunikacji HTTP/2. 

8. Argumenty wywołania procedur _add1_ i _add2_ są typu `uint32`, podobnie jest z wynikiem.
Wnioskuję (z nazwy typu), że rozmiar każdej z tych wartości to 4 bajty.

10) Wydaje mi się, że mechanizm **deadline** to mechanizm kliencki, bo to klient ustala, ile wynosi deadline
(czyli ile będzie czekał na wiadomość),
Z drugiej strony, serwer musi zadbać o "posprzątanie" po uruchomieniu usługi w przypadku przekroczenia
deadline'u (w Wiresharku widać _RST_STREAM_).

11) Nie wiem czy dobrze zrozumiałem intencje, ale dodałem tę operację jako kolejną wartość w `OperationType`,
więc finalnie tak naprawdę do interfejsu _AdvancedCalculator_, a nie _Calculator_.
"Zgłaszam błąd" (wypisuję komunikat po stronie serwera), gdy sekwencja jest pusta (_N_ wynosi 0).

12) Wygląda na to, że w obu przypadkach (dodanie kolejnej usługi implementującej ten sam interfejs IDL /
dodanie nowej usługi implementującej inny interfejs IDL) nie powoduje to żadnych problemów, jest to możliwe.
W ICE wydaje mi się, że również było to możliwe, chociaż było to inaczej realizowane (poprzez serwantów - 
specjalne obiekty --> podejście **obiektowe**).

14. Diagram interakcji HTTP/2 między klientem a serwerem jest w pliku _diagram1.png_.
Wydaje mi się, że to podejście **nie ułatwia** komunikacji, gdy klient jest "za NAT-em", ponieważ
następuje (w ramach strumienia) wysłanie wielu wiadomości od serwera do klienta.
Zakończenie wywołania strumieniowego jest sygnalizowane komunikatem HTTP/2 z flagą _End Stream_. 

15. Diagram interakcji HTTP/2 między klientem a serwerem jest w pliku _diagram2.png_.
Wydaje mi się, że to podejście z kolei **ułatwia** komunikację w sytuacji, gdy klient jest "za NAT-em", 
ponieważ serwer przesyła do klienta tylko wiadomość potwierdzającą żądanie oraz po zakończeniu 
strumieniowania (z odpowiedzią na żądanie). To klient głównie wysyła komunikaty do serwera.

16. Jeżeli chodzi o mechanizmy HTTP/2 wykorzystywane do multipleksacji żądań, to znalazłem informacje
o równoważeniu obciążenia (**load balancing**). To, co zaobserwowałem w Wiresharku, to używanie różnych
strumieni HTTP/2 (widać używane różne strumienie, jako że są różne identyfikatory strumieni).

17. Z tego co zauważyłem w Wiresharku, pakiety _PING_ są wysyłane tuż po wysłaniu żądania przez klienta
do serwera oraz od razu po tym, jak serwer wyśle odpowiedź do klienta. Pakiety te wysyłane są "parami"
(kolejno w obie strony), w moim przypadku zdarzało się, że najpierw wysyłającym był klient, w innym
przypadku serwer - nie zaobserwowałem jakiejś reguły.
Pakiety są wysyłane, aby utrzymać połączenie między klientem a serwerem (nawet wtedy, gdy nie są wysyłane 
żądania) - czyli pełnią rolę _keep alive_.

18. Jako że część z _nginx_ była oznaczona jako "dla chętnych" oraz ze względu na trudności z przygotowaniem
opisanego przypadku testowego z wykorzystaniem **reverse proxy**, ominąłem (przynajmniej na razie) ten punkt.

19. Typy ramek, jakie pojawiają się w komunikacji śledzonej w Wiresharku, to m. in. _SETTINGS_, _WINDOW_UPDATE_, 
_HEADERS_, _DATA_, _RST_STREAM_.
Opóźnienie wywołania _Add_ (w pliku _grpc-1.pcapng_) wynosi dla pierwszego wywołania około 0.004s, ale już dla
drugiego wywołania jest to więcej - około 0.15 sekundy.
Wartość deadline'u (100 ms) została podana w nagłówku HTTP (Header: grpc-timeout).
Klient wie, że strumień się zakończył, bowiem po wysłaniu wszystkich wiadomości w ramach strumienia serwer
przesyła wiadomość z flagą _End Stream_.
W przypadku, gdy to strona kliencka inicjuje wywołanie strumieniowe, serwer wie o końcu strumienia z podobnego
powodu (jest różnica - ta flaga jest dołączona do ostatniej wiadomości z danymi, nie jako oddzielna wiadomość).
