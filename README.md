VKGrep
======

прога для поиска по новостям в контакте

schema.xml положить в example/solr/collection1/conf

запуск солара:
cd solr-4.5.1/example
java -jar start.jar
важно именно войти в эту папку иначе будет ошибка

поиск делать http://localhost:8983/solr/select?q=text:apple

поиск по времени http://localhost:8983/solr/select?q=time:[1385019600 TO *]

селектится по 10 новостей

для того чтобы селектить дальше нужно дописывать &start=10
