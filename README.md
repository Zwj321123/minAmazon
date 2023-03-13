# mini amazon


A full-stack web application for simulating the communication of e-commerce systems with warehouses and logistics. The frontend was developed in **Django** and the backend in **Java**. **Google Protocol Buffer** is used to communicate with the World Simulator and Mini-UPS systems.  

author: Wenjun Zeng, Suchuan Xing

## How to run
docker compose up

## Feature checklist(requirement)

- [x] Buy products(communicate with both the world and UPS).
- [x] Different categories of products.
- [x] Check the status of an order.
- [x] Specify the deliver address(i.e. (x,y)).
- [x] Specify a UPS account name to associate the order with.
- [x] Provide a *tracking number* for each shipment.

## Extra features we have

- [x] A **well-developed shopping cart**.
    - checkout all orders in cart
    - dynamically display the number of products in cart
    - remove any items in the cart
    - the price will change according to user's actions(e.g., delete items, add items)
    - Users can add items to the cart without logging in, and the cart will remember the items in it after logging in.
- [x] Search bar in home page.
    - search products by categories
    - search products by product titles
    - search products by descriptions
- [x] A **full-featured order page**.
    - search bar --- locate any order by item name
    - view detail of any orders
- [x] Build-in data
    - use **signals** to make sure our MiniAmazon system has some build-in data(e.g. initial items, defualt user), easy to deploy
- [x] Product category.
    - Search by category of products, can switch between them in the home page
- [x] Associate your amazon account with your UPS account.
    - automatically associate each order with your UPS account
- [x] User-friendly UI and interaction.
    - all edit info page will have some error handling, will show the error message if failed
