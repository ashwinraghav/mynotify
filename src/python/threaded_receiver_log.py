#!/usr/bin/env python
import pika
import threading

THREADS = 100

class Receive(threading.Thread):
    def __init__(self,index):
        threading.Thread.__init__(self)
        self.index = index
        connection = pika.BlockingConnection(pika.ConnectionParameters(host='elmer.cs.virginia.edu'))
        self.channel = connection.channel()
        self.channel.exchange_declare(exchange='logs'+str(index),type='fanout')
        result = self.channel.queue_declare(exclusive=True)        
        queue_name = result.method.queue
        self.channel.queue_bind(exchange='logs'+str(index),
                   queue=queue_name)
        print '['+str(index)+']'+'Waiting for logs. To exit press CTRL+C'
        self.channel.basic_consume(callback,
                      queue=queue_name,
                      no_ack=True)  
    def run(self):
        self.channel.start_consuming()
            
def callback(ch, method, properties, body):
    print " [Received] %r" % (body,)

threadLock = threading.Lock()
threads = []
for i in range(THREADS):
    threads.append(Receive(i))

for i in range(THREADS):
    threads[i].start()