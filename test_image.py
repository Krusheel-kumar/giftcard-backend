import urllib.request
urls = [
    "https://raw.githubusercontent.com/Krusheel-kumar/giftcard-customer-ui/main/src/assets/20off.jpeg",
    "https://raw.githubusercontent.com/Krusheel-kumar/giftcard-customer-ui/main/src/assets/bobafood.jpeg"
]
for url in urls:
    try:
        response = urllib.request.urlopen(url)
        data = response.read()
        print(url, "Downloaded bytes:", len(data))
    except Exception as e:
        print(url, e)

