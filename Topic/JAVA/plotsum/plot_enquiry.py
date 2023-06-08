import requests
import json

def main():
    baidu_token = open("OCR/JAVA/plot_sum/baidu_key.txt", "r")
    baidu_token.readline() # skip the first line
    API_KEY = baidu_token.readline().strip()
    SECRET_KEY = baidu_token.readline().strip()
        
    url = "https://aip.baidubce.com/rpc/2.0/nlp/v1/txt_monet?access_token=" + get_access_token(API_KEY, SECRET_KEY)
    
    payload = json.dumps(
        {
            "content_list": [
                {
                    "content": "一九四七年冬，东北某铁路工厂为支援解放战争，接受了抢修松花江铁桥的任务。总工程师看不到群众的力量，对完成任务缺乏信心。少数工人当中也存在着雇佣思想。为制造修复铁桥需要的桥座和铆钉，首先必须修复炼钢炉。为此，工人梁日升创造了以耐火砖代替白云石的办法，试验成功。之后，他们又克服一系列困难，制出修复铁桥需要的桥座和铆钉。为了赶修铁桥，铁路工厂的工人们又响应上级号召，参加了修桥工作，终于在江水解冻之前，将大桥修复。",
                    "query_list": [
                        {
                            "query": "时间"
                        },
                        {
                            "query": "地点"
                        }
                    ]
                }
            ]
	}
    )
    headers = {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
    
    response = requests.request("POST", url, headers=headers, data=payload)
    
    print(response.text)
    

def get_access_token(key, sec):
    url = "https://aip.baidubce.com/oauth/2.0/token"
    params = {"grant_type": "client_credentials", "client_id": key, "client_secret": sec}
    return str(requests.post(url, params=params).json().get("access_token"))

if __name__ == '__main__':
    main()
