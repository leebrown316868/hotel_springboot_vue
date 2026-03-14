#!/bin/bash

TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJpZCI6MSwic3ViIjoiYWRtaW5AaG90ZWwuY29tIiwiaWF0IjoxNzczNTAyMTY4LCJleHAiOjE3NzYwOTQxNjh9.E8mUj92smiT-3TbXS2fot39BN1Q4KBCd2pfM593x0gw"
BASE_URL="http://localhost:8080/api/guests"

# 中国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Wang Wu","email":"wangwu@example.com","phone":"13700137000","country":"China","status":"ACTIVE","lastStay":"2023-10-25"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Chen Wei","email":"chen.wei@example.cn","phone":"13500135000","country":"China","status":"VIP","lastStay":"2023-11-05"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Zhao Min","email":"zhaomin@example.cn","phone":"13600136000","country":"China","status":"INACTIVE","lastStay":"2023-05-10"}'

# 日本客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Yuko Tanaka","email":"y.tanaka@example.jp","phone":"+81 3 1234 5678","country":"Japan","status":"ACTIVE","lastStay":"2023-09-15"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Taro Yamada","email":"t.yamada@example.jp","phone":"+81 90 1234 5678","country":"Japan","status":"ACTIVE","lastStay":"2023-10-20"}'

# 韩国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Jisu Kim","email":"j.kim@example.kr","phone":"+82 10 1234 5678","country":"South Korea","status":"ACTIVE","lastStay":"2023-10-18"}'

# 德国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Hans Mueller","email":"h.mueller@example.de","phone":"+49 30 1234567","country":"Germany","status":"ACTIVE","lastStay":"2023-08-20"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Anna Schmidt","email":"a.schmidt@example.de","phone":"+49 171 1234567","country":"Germany","status":"INACTIVE","lastStay":"2023-06-15"}'

# 法国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Sophie Martin","email":"s.martin@example.fr","phone":"+33 1 23 45 67 89","country":"France","status":"INACTIVE","lastStay":"2023-10-10"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Pierre Dupont","email":"p.dupont@example.fr","phone":"+33 6 12 34 56 78","country":"France","status":"ACTIVE","lastStay":"2023-10-28"}'

# 美国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"John Smith","email":"j.smith@example.com","phone":"+1 212 555 1234","country":"USA","status":"VIP","lastStay":"2023-11-02"}'
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Amy Johnson","email":"a.johnson@example.com","phone":"+1 310 555 9876","country":"USA","status":"ACTIVE","lastStay":"2023-10-30"}'

# 英国客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"James Wilson","email":"j.wilson@example.co.uk","phone":"+44 20 7946 0958","country":"UK","status":"ACTIVE","lastStay":"2023-10-22"}'

# 澳大利亚客户
curl -s -X POST "$BASE_URL" -H "Authorization: Bearer $TOKEN" -H "Content-Type: application/json" -d '{"name":"Emma Brown","email":"e.brown@example.com.au","phone":"+61 2 9876 5432","country":"Australia","status":"ACTIVE","lastStay":"2023-10-25"}'

echo "All guests created successfully!"
