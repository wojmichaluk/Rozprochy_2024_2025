Odpowiedzi na wybrane punkty z części Serializacja Protocol Buffers (2.3)

6. Pojedyncza serializacja trwa około 0.4 mikrosekundy.
Serializowane dane zostały zapisane na 98 bajtach.

7. Porównanie z innymi sposobami serializacji (czas i wielkość):
- _Fury Java_ - około 1 mikrosekunda, 157 bajtów,
- domyślna serializacja Javy: około 6.3 mikrosekundy, 418 bajtów,
- serializacja tekstowa (JSON): około 2.2 mikrosekundy, 210 bajtów.

Z tych sposobów serializacji najlepsza (najszybsza, serializuje na najmniejszej liczbę bajtów) 
jest _Fury Java_. Natomiast i tak lepsza jest od nich serializacja **protobuf**.

8. Zdekodowana wiadomość proto **nie zawiera** drugiego pola drugiego numeru telefonu, tzn.
informacji o typie numeru telefonu (_MOBILE_, wartość 0).

9. Podobnie jak poprzednio, zostało "ucięte" jedno pole. Jest też wiele różnic:
- zamiast kolejnych **numerów** pól (1:, 2:, ...), są ich nazwy (_name_, _id_, ...),
- wartość _incomePercentage_ (pole 4) jest poprawnie wyświetlana, rozpoznana jako liczba zmiennoprzecinkowa,

Różnica wynika stąd, że w pewnym sensie podaliśmy dekoderowi "instrukcję" do dekodowania, więc kolejne pola
zostały rozpoznane na jej podstawie, wraz z nazwami i typem, porównując do "surowego" dekodowania.

10. Po dodaniu nowej wiadomości i uwzględnieniu w tworzonym obiekcie 3 liczb w sekwencji, czas serializacji
wzrósł do około 0.54 mikrosekundy, a długość wiadomości po serializacji wzrosła do 126 bajtów.
