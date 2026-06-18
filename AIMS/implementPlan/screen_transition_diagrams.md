# Screen Transition Diagrams

Here are the four core flows broken down into **Simple Text Flows** (easy for self-drawing on Draw.io/Figma) and their corresponding **PlantUML Code**.

---

## Flow 1: Authentication & User Management
*Transition Pattern: Hub and Spoke / Sequential*

**Simple Flow for Self-Drawing:**
1. **Login View** is the starting point.
2. From Login View, user can click "Login" -> transitions to **Homepage View** (if customer) OR **User/Product Management View** (if Admin).
3. From Login View, user can click "Change Password" -> transitions to **Change Password View**.
4. From User Management View, admin clicks "Add/Edit User" -> transitions to **User Form View**.

**PlantUML Code:**
```plantuml
@startuml
skinparam handwritten false
skinparam monochrome true

[*] --> LoginView : Start Application

state LoginView {
}
state HomepageView {
}
state ChangePasswordView {
}
state UserManagementView {
}
state UserFormView {
}

LoginView --> HomepageView : Click "Login" (Customer)
LoginView --> UserManagementView : Click "Login" (Admin)
LoginView --> ChangePasswordView : Click "Change Password"

UserManagementView --> UserFormView : Click "Add/Edit User"
UserFormView --> UserManagementView : Click "Save/Cancel"

@enduml
```

---

## Flow 2: Customer Shopping Flow
*Transition Pattern: Sequential (with some Hub and Spoke from Homepage)*

**Simple Flow for Self-Drawing:**
1. Start at **Homepage View**.
2. Click on a product -> **Product Detail UI View**. (Can go back to Homepage).
3. From Homepage or Product Detail, click "Cart" -> **Cart View**.
4. From Cart View, click "Place Order" -> **Place Order View**.
5. From Place Order View, click "Delivery Info" -> **Delivery Info Dialog** (Modal).
6. After Delivery Info, proceed to -> **Payment View**.
7. After Payment success -> **Order Success View**.
8. From Order Success View, view -> **Invoice Dialog**.

**PlantUML Code:**
```plantuml
@startuml
skinparam monochrome true

state HomepageView
state ProductDetailView
state CartView
state PlaceOrderView
state DeliveryInfoDialog <<Modal>>
state PaymentView
state OrderSuccessView
state InvoiceDialog <<Modal>>

HomepageView --> ProductDetailView : Click Product
ProductDetailView --> HomepageView : Back

HomepageView --> CartView : Click Cart Icon
ProductDetailView --> CartView : Add to Cart / View Cart

CartView --> PlaceOrderView : Click "Place Order"
PlaceOrderView --> CartView : Back

PlaceOrderView --> DeliveryInfoDialog : Enter Delivery Info
DeliveryInfoDialog --> PlaceOrderView : Confirm/Cancel

PlaceOrderView --> PaymentView : Proceed to Payment
PaymentView --> OrderSuccessView : Payment Successful
OrderSuccessView --> InvoiceDialog : View Invoice
InvoiceDialog --> HomepageView : Return to Home

@enduml
```

---

## Flow 3: Admin Product Management
*Transition Pattern: Hub and Spoke*

**Simple Flow for Self-Drawing:**
1. Start at **Product List View** (The Hub).
2. Click "Add Product" -> **Product Form View**.
   - On Save -> **Product Success Dialog**, then back to Product List View.
3. Click "Edit Product" -> **Product Update Form View**.
   - On Update -> **Product Update Success Dialog**, then back to Product List View.
4. Click "View History" -> **Product History View**, then back to Product List View.

**PlantUML Code:**
```plantuml
@startuml
skinparam monochrome true

state ProductListView
state ProductFormView
state ProductUpdateFormView
state ProductHistoryView
state ProductSuccessDialog <<Modal>>
state ProductUpdateSuccessDialog <<Modal>>

ProductListView --> ProductFormView : Click "Add Product"
ProductFormView --> ProductSuccessDialog : Click "Save"
ProductSuccessDialog --> ProductListView : Close

ProductListView --> ProductUpdateFormView : Click "Edit Product"
ProductUpdateFormView --> ProductUpdateSuccessDialog : Click "Update"
ProductUpdateSuccessDialog --> ProductListView : Close

ProductListView --> ProductHistoryView : Click "View History"
ProductHistoryView --> ProductListView : Back

@enduml
```

---

## Flow 4: Admin Order Management
*Transition Pattern: Hub and Spoke*

**Simple Flow for Self-Drawing:**
1. Start at **Order Management View**.
2. Click "View Details" on an order -> **Order Detail Dialog** (Modal).
3. From Order Detail Dialog (or list), click "Reject Order" -> **Order Reject Dialog** (Modal).
4. After Rejecting, return to **Order Management View**.

**PlantUML Code:**
```plantuml
@startuml
skinparam monochrome true

state OrderManagementView
state OrderDetailDialog <<Modal>>
state OrderRejectDialog <<Modal>>

OrderManagementView --> OrderDetailDialog : Click "View Details"
OrderDetailDialog --> OrderManagementView : Close

OrderManagementView --> OrderRejectDialog : Click "Reject Order"
OrderRejectDialog --> OrderManagementView : Confirm Reject / Cancel

@enduml
```
