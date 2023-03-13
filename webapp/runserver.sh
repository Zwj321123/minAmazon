#!/bin/bash
while [ "1"=="1" ]
do
    #send yes to the input of manage.py
    echo yes | python3 manage.py collectstatic
    python3 manage.py runserver 0.0.0.0:8000
    sleep 1
done