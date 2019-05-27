FROM ubuntu:latest
MAINTAINER Shazni Nazeer "shazni@gmail.com"

RUN apt-get update -y
RUN apt-get install -y python-pip python-dev build-essential

COPY . /app
WORKDIR /app
RUN pip install -r requirements.txt
WORKDIR /app/api

ENTRYPOINT ["python"]
CMD ["api.py"]
