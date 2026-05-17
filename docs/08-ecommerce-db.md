# DB Design for E-commerce

## Main Entities

The main entities are:

- users
- products
- carts
- orders
- inventory

---

# Relationships

- one user can place many orders
- one order can contain multiple products
- one product belongs to inventory
- one user can have one active cart

---

# Schema Design

## Users

Stores:
- user profile
- login details
- addresses

---

## Products

Stores:
- product title
- description
- category
- price

---

## Orders

Stores:
- user_id
- order status
- payment details
- total amount

---

## Cart

Stores:
- temporary user selections before checkout

---

## Inventory

Stores:
- stock quantity
- warehouse availability

---

# Important Indexes

## users(email)

Used for:
- login
- authentication

---

## orders(user_id)

Used for:
- fetching order history

---

## products(category)

Used for:
- category filtering

---

## inventory(product_id)

Used for:
- fast stock lookup

---

# Denormalization Decision

The orders table stores:
- total_price

instead of calculating it repeatedly.

Why:
- faster order history reads
- avoids repeated aggregation queries

Trade-off:
- duplicated data
- requires consistency during updates

---

# Scaling Considerations

- use CDN for product images
- cache popular products using Redis
- partition very large orders table
- use read replicas for analytics queries
- use async queues for notifications