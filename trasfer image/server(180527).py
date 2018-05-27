# -*- coding: utf-8 -*-

import socketserver
from os.path import exists

HOST = ''
PORT = 9999

class MyTcpHandler(socketserver.BaseRequestHandler):
    def getFileFromClient(self):
        data_transferred = 0
        data = self.request.recv(1024)
        print("안녕")
        print(data)
        if not data:
            print('서버에 존재하지 않거나 전송중 오류발생')
            return
        
        filename='test'
        
        with open('download/' + filename+".jpg", 'wb') as f:
            try:
                while data:
                    #print(data)
                    f.write(data)
                    data_transferred += len(data)
                    data = self.request.recv(1024)
            except Exception as e:
                print(e)

        print('파일[%s] 전송종료. 전송량 [%d]' % (filename, data_transferred))


    def handle(self):     
        print('[%s] 연결됨' % self.client_address[0])

        type = self.request.recv(1024)
        print(type)
        type = type.decode()
        if type == 'Send\n':
            self.getFileFromClient()
        else:
            self.SendFileToClient()
        #함수 호출
        
        #self.SendFileToClient()
        
        
        
    def SendFileToClient(self):
        data_transferred = 0
        filename='clock_ori.png' #파일 임의로 지정해줘서 보여지는지 확인
        if not exists(filename):  # 파일이 해당 디렉터리에 존재하지 않으면
            return  # handle()함수를 빠져 나온다.

        print('파일[%s] 전송 시작...' % filename)
        with open(filename, 'rb') as f:
            try:
                data = f.read(1024)  # 파일을 1024바이트 읽음
                while data:  # 파일이 빈 문자열일때까지 반복
                    data_transferred += self.request.send(data)
                    data = f.read(1024)
            except Exception as e:
                print(e)

        print('전송완료[%s], 전송량[%d]' % (filename, data_transferred))


def runServer():
    print('++++++파일 서버를 시작++++++')
    print("+++파일 서버를 끝내려면 'Ctrl + C'를 누르세요.")

    try:
        server = socketserver.TCPServer((HOST, PORT), MyTcpHandler)
        server.serve_forever()
    except KeyboardInterrupt:
        print('++++++파일 서버를 종료합니다.++++++')


runServer()