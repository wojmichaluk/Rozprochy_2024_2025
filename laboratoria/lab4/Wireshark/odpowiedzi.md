Odpowiedzi na wybrane punkty z części Podstawy protokołu HTTP/2 (2.2)

4. Klient żąda protokołu h2 (HTTP/2). Protokołem drugiego wyboru klienta jest http/1.1.
Serwer wybrał protokół h2 (HTTP/2).

6) Sesja HTTP kończy się przez pakiet _GOAWAY_ (na strumieniu inicjowanym przez serwer).

8. Załadowałem stronę [kursu Systemów Rozproszonych na Upelu](https://upel.agh.edu.pl/course/view.php?id=3084).
Dominującą wartością w kolumnie **Protocol** jest http/1.1, więc wnioskuję, że właśnie ta wersja protokołu HTTP jest wykorzystywana.

9. Wartość _h3_ w kolumnie **Protocol** zapewne oznacza obsługę protokołu HTTP/3.
Na wymienionej stronie zdarza się taka sytuacja.

Co oznacza **Alt-Svc: h3=":443"; ma=2592000,h3-29=":443"; ma=2592000**?
- _Alt-Svc_ to skrót od "Alternate Service",
- pary wartości `<protocol_id>=<alt-authority>`: pierwszy element to identyfikator protokołu z ALPN (tutaj h3, h3-29 -> _draft 29 of HTTP/3 protocol_), a drugi składa się z opcjonalnego _host override_ oraz wymaganego numeru portu,
- _ma_ oznacza "max age", wyrażone w sekundach.

10. Dwie strony WWW, dla których obsługi:
- działa protokół HTTP/2:
- - [Google](https://www.google.pl),
- - [Jeja](https://www.jeja.pl).
- nadal jest używany protokół HTTP/1.1:
- - [Upel AGH](https://upel.agh.edu.pl),
- - [Usos AGH](https://web.usos.agh.edu.pl).

[Główna strona AGH](https://www.agh.edu.pl/) **nie** obsługuje HTTP/2. 
