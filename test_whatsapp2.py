import urllib.request, json

url = "https://api.msg91.com/api/v5/whatsapp/whatsapp-outbound-message/bulk/"
auth_key = "557539A3jnNLJWr6a73367fP1"

payload = {
    "integrated_number": "917794971935",
    "content_type": "template",
    "payload": {
        "messaging_product": "whatsapp",
        "type": "template",
        "template": {
            "name": "gift_card_delivery",
            "language": {
                "code": "en",
                "policy": "deterministic"
            },
            "to_and_components": [
                {
                    "to": ["917794971935"],
                    "components": {
                        "header_1": {
                            "type": "image",
                            "value": "https://raw.githubusercontent.com/Krusheel-kumar/giftcard-customer-ui/main/src/assets/buyonegetone.jpeg"
                        },
                        "body_1": {"type": "text", "value": "Test User"},
                        "body_2": {"type": "text", "value": "Buy 1 Get 1"},
                        "body_3": {"type": "text", "value": "TEST-CODE"},
                        "body_4": {"type": "text", "value": "2026-10-10"}
                    }
                }
            ]
        }
    }
}

req = urllib.request.Request(url, data=json.dumps(payload).encode("utf-8"), headers={
    "Content-Type": "application/json",
    "authkey": auth_key
})

try:
    with urllib.request.urlopen(req) as response:
        print(response.read().decode("utf-8"))
except urllib.error.HTTPError as e:
    print(e.read().decode("utf-8"))

