# 账单接口 Postman 操作步骤

## 一、准备工作

1. 启动后端项目，接口基础地址为 `http://localhost:8080`。
2. 在 Postman 中创建一个 Collection，命名为“账单接口 CRUD”。
3. 先调用登录接口：
   - 请求方式：`POST`
   - URL：`http://localhost:8080/api/users/login`
   - Body 选择 `raw` 和 `JSON`，填写：

```json
{
  "phone": "13800000001",
  "password": "123456"
}
```

4. 从登录响应中复制 `token`。
5. 后续账单接口均在 `Authorization` 中选择 `Bearer Token`，粘贴登录返回的 token。

## 二、新增账单

1. 请求方式选择 `POST`。
2. URL 填写 `http://localhost:8080/api/records`。
3. 在 `Authorization` 中配置 Bearer Token。
4. Body 选择 `raw` 和 `JSON`，填写：

```json
{
  "type": "EXPENSE",
  "category": "餐饮",
  "amount": 28.50,
  "recordDate": "2026-06-12",
  "remark": "Postman新增测试账单"
}
```

5. 点击 `Send`，响应中的 `id` 是后续修改和删除使用的账单编号。

## 三、查询账单

1. 请求方式选择 `GET`。
2. URL 填写 `http://localhost:8080/api/records?month=2026-06`。
3. 在 `Authorization` 中配置 Bearer Token。
4. 点击 `Send`，响应为该月份的账单数组。

## 四、修改账单

1. 请求方式选择 `PUT`。
2. URL 填写 `http://localhost:8080/api/records/12`，其中 `12` 替换为新增接口返回的实际 `id`。
3. 在 `Authorization` 中配置 Bearer Token。
4. Body 选择 `raw` 和 `JSON`，填写修改后的完整账单数据：

```json
{
  "type": "EXPENSE",
  "category": "交通",
  "amount": 35.00,
  "recordDate": "2026-06-12",
  "remark": "Postman修改后的账单"
}
```

5. 点击 `Send`，响应为修改后的账单。

## 五、删除账单

1. 请求方式选择 `DELETE`。
2. URL 填写 `http://localhost:8080/api/records/12`，其中 `12` 替换为要删除的实际账单 `id`。
3. 在 `Authorization` 中配置 Bearer Token。
4. 无需填写 Body，点击 `Send`。
5. 返回 `{"message":"删除成功"}` 表示删除成功。

## 字段说明

| 字段 | 含义 | 示例 |
|---|---|---|
| `type` | 账单类型，只能是收入或支出 | `INCOME` / `EXPENSE` |
| `category` | 账单分类 | `餐饮` |
| `amount` | 金额，必须大于等于 0.01 | `28.50` |
| `recordDate` | 账单日期，格式为 yyyy-MM-dd | `2026-06-12` |
| `remark` | 备注，最多 500 个字符 | `午餐` |
