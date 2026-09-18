#! /usr/bin/env python3


from http import client

def test_http_servlet():
    conn = client.HTTPConnection(host='localhost', port=42069)
    
    try: 
        conn.request('GET', '/')
        res = conn.getresponse()

        assert res.status == 200

    finally:
        conn.close()

