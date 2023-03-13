FROM python:3
ENV PYTHONUNBUFFERED 1
RUN mkdir /code
WORKDIR /code
ADD requirements.txt /code/
RUN pip install --upgrade pip
RUN pip install -r requirements.txt
ADD . /code/
RUN chmod -R 777 ./static_cdn/media_root/amazon
RUN chmod -R 777 ./static_cdn/static_root