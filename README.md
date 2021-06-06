RU
Представляю Вашему вниманию программу Cloud Storage
Прикладная программа для хранения файлов в удаленном хранилище

1. Установка и запуск 

    1.1 Скопируйте на компьютер-клиент файл Cloud Client, а на компьютер-сервер Cloud Server
    
    1.2 Запустите Cloud Server на сервере. Для проверки работоспособности откройте диспетчер задач и найдите среди процессов Java Binary SE.
    
    1.3 Запустите Cloud Client на клиенте. После запуска откроется окно программы и соединение с сервером произойдёт автоматически. 
        Если сервер по какой-то причине отключился, есть кнопка повторного подключения Restart Connection.

2. Руководство по использованию
   
    2.1 Для регистрации новой учётной записи нажмите кнопку Registration. В появившихся полях введите свои логин, пароль и никнейм.
        Нажмите ОК для подтверждения Cancel для отмены. В последствии никнейм можно будет изменить. 
        После успешной регистрации в окне информации внизу будет отображено подтверждение.
        Далее автоматически будут созданы директории server_+login на сервере в папке с файлом Cloud Server и client_+login на клиенте в папке Cloud Client, где login это логин пользователя.
    
    2.2 Для входа в учётную запись нажмите кнопку Authentication. В появившихся полях введите логин и пароль.
        Нажмите ОК для подтверждения Cancel для отмены.
        В случае успешного входа в учётную запись в заголовке окна появится никнейм пользователя.
        Если по какой-то причине директории server_+login на сервере в папке с файлом Cloud Server и client_+login на клиенте в папке с файлом Cloud Client были удалены или перенесены, то они будут созданы повторно автоматически.
        Созданные директории будут отображены в двух таблицах в центре окна программы: слева client_+login, справа server_+login.
    
    2.3 При нажатии на кнопку Change Nick появится окно, куда нужно будет ввести новый никнейм.
        Нажмите ОК для подтверждения Cancel для отмены.
        При успешной смене никнейма пользователя, новый никнейм появится в заголовке окна.
    
    2.4 Для создания файла напишите имя файла в соответствующем поле, выберите директорию и нажмите Create File. 
        Если файл с таким именем уже есть, то новый файл не будет создан. 
        В случае если не ввести имя файла, будет создан новый файл с именем new file(n), где n счётчик новых файлов без имён. Обнуляется при новом запуске программы
    
    2.5 Для создания директории напишите имя директории в соответствующем поле, выберите директорию и нажмите Create Directory
        Если директория с таким именем уже есть, то новая директория не будет создана.
        В случае если не ввести имя директории, будет создан новый файл с именем New Folder(n), где n счётчик новых директорий без имён. Счетчик новых файлов обнуляется при новом запуске программы.
    
    2.6 Для переноса файла на сервер выберите файл или директорию на стороне клиента (слева) и нажмите Upload. 
        Файл или директория появится на стороне сервера (справа) и удалится на стороне клиента  
   
    2.7 Для переноса файла на клиент выберите файл или директорию на стороне сервера (справа) и нажмите Download.
        Файл или директория появится на стороне клиента (слева) и удалится на стороне сервера

    2.8 Для копирования файла или директории выберите файл или директорию на сервере или клиенте (справа или слева) и нажмите Copy File.
        Копия файла появится на противоположной стороне (сервере или клиенте в зависимости от выбора)

    2.9 Для удаления файла или директории выберите файл или директорию на клиенте или сервере (справа или слева) и нажмите Delete File.

    2.10 Для просмотра содержимого файла или директории выберите файл или директорию на клиенте или сервере (справа или слева) и нажмите Show File.
        Содержимое файла отобразится в окне информации, при выборе директории отобразится список файлов директории.
   
    2.11 Для поиска файла напишите имя файла в соответствующем поле затем нажмите Search File.
         При обнаружении файла абсолютный путь файла появится в окне информации
   
    2.12 При нажатии на кнопку Up произойдет переход по директории вверх

    2.13 В случае ошибок пользователь будет оповещён в окне информации или во всплывающем Alert окне
   
3. Описание программы:

    3.1 Программа разработана на языке программирования Java версии 8. 
        Клиент-серверная связь реализована с помощью фреймворка Netty версии 4.1.59 и NIO. 
        Интерфейс пользователя реализован с помощью JavaFX
        Сборка проекта реализована на Maven версии 3.8.1
        База данных создана на MySQL версии 5.7
    
    3.2 Работа программы основана на отправке сообщений-команд на сервер.
        В свою очередь сервер предоставляет адреса директорий, которые обрабатываются на клиенте.

    3.3 Регистрация пользователя, вход и смена никнейма присходят при взаимодействии сервера с базой данных

    3.4 Создание, копирование, удаление, поиск файлов реализованы на NIO
        Сортировать файлы возможно по размеру, по типу, по дате изменения и по имени 

4. Источники информации
   
    При разработке проекта использовались вебинары от GeekBrains на YouTube.
    Статьи на StackOverflow и Хабр
   