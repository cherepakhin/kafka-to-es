# Передатчик из kafka в elasticsearch

ELASTICSEARCH_HOSTS:10.98.140.6:9200

####Переменные окружения для настройки

|Назначение|Имя пременной|Значение по умолчанию|
|---|---|---|
|Порт для проверки|SERVER_PORT|8081|
|Адрес Kafla|KAFKA_HOST|v.perm.ru:9093|
|Топик Kafla|KAFKA_TOPIC|er-log|
|Имя группы kafka для нескольких consumers|KAFKA_NAME_GROUP|logger|
|Хост ElastcSearch|ELASTICSEARCH_HOSTS|192.168.1.20:9200|
|Имя индекса ElastcSearch|ELASTICSEARCH_NAME_INDEX|device|
|Размер буфера клиента ElastcSearch в документах|COUNT_DOCS|10000|
|Размер буфера клиента ElastcSearch в мегабайтах|SIZE_QUEUE_MBS|5|
|Интервал между запросами клиента ElastcSearch|FLUSH_INTERVAT_SECONDS|20|
|Кол-во конкурентных запросов клиента ElastcSearch|COUNT_CONCURENT_REQUESTS|1|
