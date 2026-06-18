# UI Design Report Preparation Plan

This document outlines the detailed plan and template for the **UI Design** section of the AIMS Project Report. It has been updated to incorporate your specific requirements for configuration standards, transition diagram methodologies, and detailed screen specification tables.

## 1. Screen Configuration Standardization

**Objective:** Define consistent guidelines for visual appearance, layout, controls, and system behaviors across the AIMS application.

### Content to Prepare & Define:

*   **Display & Window Standards:**
    *   **Display:** Specify the physical size, resolution, and the number of colors supported by the target displays.
    *   **Screen Division:** Detail how the screen is divided into displayed objects called windows (e.g., main application window vs. modal dialogs like `invoice-dialog.fxml`).
    *   **Location of Standard Buttons:** Standardize where standard buttons (e.g., OK, Cancel, Register, Search) should be placed across all screens.
    *   **Location of Messages:** Define the display location of system messages, notifications, and alerts.
    *   **Titles & Menus:** Establish the standard display format for screen titles and navigation menus.

*   **Text & Terminology:**
    *   **Consistency:** Maintain consistency in the expression of alphanumeric characters.
    *   **Phrasing:** Standardize the expression of sentences and detailed items across the UI.

*   **Aesthetics & Controls:**
    *   **Color Coordination:** Define a cohesive color palette for the application.
    *   **Controls:** Standardize the style, size, color, and characters displayed for UI controls.

*   **Interaction & Behavior:**
    *   **Input Check Process:** Standardize the input check (validation) process before submitting forms.
    *   **Focus Sequence:** Define the sequence of moving the focus (e.g., defining a logical tab sequence across input forms).
    *   **Direct Input:** Guidelines for direct input from a keyboard.
    *   **Shortcut Keys:** Maintain consistency in the assignment of shortcut keys throughout the app.

*   **Menus:**
    *   Design menus with consideration of the standard specification (common client area) of the screen.

*   **System Feedback:**
    *   **Messages:** Determine how messages are displayed when a time-consuming process is executed (e.g., a "busy" or loading indicator).
    *   **Error Handling:** Execute standardized processing and displays if an error occurs.
    *   **Help System:** Develop detailed Help information in accordance with the manual, and maintain consistency in terminology, descriptions, and explanations of methods.

---

## 2. Screen Transition Diagrams

**Objective:** Visually map user navigation across the application using standardized diagramming tools and methodologies.

### Methodology & Tools
*   **Transition Patterns:** Apply the **four transition patterns** to draw the diagrams (e.g., determining which flows are hierarchical, sequential, hub-and-spoke, etc.).
*   **Tools:** Use **PlantUML** (to generate code) or **Draw.io** to draw the diagrams.

### Core Flows to Diagram:
*   **Flow 1: Authentication & User Management** (`login-view`, `homepage-view`, `user-management-view`)
*   **Flow 2: Customer Shopping Flow** (`homepage-view`, `product-detail-ui-view`, `cart-view`, `place-order-view`, `payment-view`)
*   **Flow 3: Admin Product Management** (`product-list-view`, `product-form-view`)
*   **Flow 4: Admin Order Management** (`order-management-view`, `order-detail-dialog`)

> [!TIP]
> If you decide to use **PlantUML**, let me know and I can generate the raw PlantUML code for these specific flows based on your project's `.fxml` structure!

---

## 3. Screen Specifications

**Objective:** Provide an exhaustive, easy-to-follow technical specification for every screen in the application.

### Required Information for Each Screen:

**1. Screen Image**
*   This is the screen image to be displayed. If screen images are created in advance with the screen design tool (e.g., Scene Builder), attach a hardcopy.

**2. List of Functions**
*   Defines the names of parts such as the buttons on the screen, and summarizes their functions.

**3. Detailed Specifications**
*   Provide descriptions of events for individual screens, attributes of parts, input check specifications, and output specifications, etc.
*   Defining the field attributes.

### Screen Specification Template

*(Everything collected about the Screen Specification should be put into this easy-to-edit table)*

**Screen Name / ID:** `[e.g., Place Order View / SCR_PLACE_ORDER]`
**Screen Image:** `[Attach Hardcopy / Image Here]`

| Element ID / Name | Type / Field Attributes | Description & Function (List of functions) | Events & Actions | Input Check Specifications | Output Specifications |
| :--- | :--- | :--- | :--- | :--- | :--- |
| `btnCheckout` | Button | Proceeds to payment/delivery (Standard Button Location) | OnClick: Triggers validation & transition | N/A | N/A |
| `txtCardNumber` | TextField (Max 16 chars) | Input for payment card number | OnFocus: Move focus. OnTab: Move to next | Must be 16 digits, alphanumeric expression check | N/A |
| `lblErrorMsg` | Label (Red, Standard text) | Displays error message location | N/A | N/A | Displays standardized error on validation fail |
| `tableCart` | TableView | Displays cart items | OnRowSelect: Highlight | N/A | Binds to `CartItem` model |

*Copy and paste the above layout for all screens like `homepage-view.fxml`, `payment-view.fxml`, `user-management-view.fxml`, etc.*
