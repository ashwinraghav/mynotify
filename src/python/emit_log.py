#!/usr/bin/env python
import pika
import sys

RECEIVERS = 100

connection = pika.BlockingConnection(pika.ConnectionParameters(
        host='elmer.cs.virginia.edu'))
channel = connection.channel()

#create 100 distinct exchanges called 'logs<index>'
for i in range(RECEIVERS):
    channel.exchange_declare(exchange='logs'+str(i),type='fanout')


message = "info: Hello Receiver!"

#Publish message with preprended index
for i in range(100):
    send_message = str(i)+": "+message
    channel.basic_publish(exchange='logs'+str(i),routing_key='',body=send_message)
    
print " [x] Sent %r" % (message,)
connection.close()