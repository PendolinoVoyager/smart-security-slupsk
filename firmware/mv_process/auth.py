import os

TOKEN_PATH = "/etc/sss_firmware/token.txt"
DEBUG_TOKEN = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlzRGV2aWNlIjp0cnVlLCJkZXZpY2VVdWlkIjoiRGVmYXVsdF9VVUlEIiwiaWF0IjoxNzM4MDAxOTI2fQ.vG6uiRnPWKnKZLi5mVLZNuFhKq8hUb1r0kqf-qjsazGyyvRdIMEM67yDYtdJ9gehUXtcuaObW6XO0AaiJPkz5Mjk_fdyRPOUvw-6FEjanV4r2jFLh79dSt2dFGcD0IOuEsS5j9WJvR9e_hUB7YbykpF17YgNvPnBwDsErT-P9NrOj9Hp8u3xIvlzs4RJ4ypwYi6rQleQwiXFwpS5qdXg1M8lZM6CJNSNdEzew1ab8Y25F3Ynd_VxoWjfj2kElinWh7H28NP8FJpibHqKo5CbYvSeLwR6tFP6scnGQR-zUZ063D3jA1fk5VLNHaYciaWTdCkHxZug7pxz6lvQFIemCA"

def get_token():
    if os.environ.get('MV_DEBUG') == "1":
        return DEBUG_TOKEN

    file = open(TOKEN_PATH, "r")
    return file.readline()
