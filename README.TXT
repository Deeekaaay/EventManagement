
============================
How to Use This Application
============================

1. **Login & Signup**
   - On launch, you will see the login screen.
   - New users can click 'Sign up' to create an account. Passwords are securely encrypted.
   - Existing users (including admin) can log in with their credentials.

2. **User Home Page**
   - After login, normal users see the Home page with a list of all available events.
   - You can add events to your cart, update or remove them, and proceed to checkout.
   - Checkout requires a 6-digit confirmation code and validates event dates and seat availability.
   - After successful checkout, event seat counts are updated and your order is saved.

3. **Account Options (User)**
   - At the top of the Home page, there is an 'Account' menu (MenuBar).
   - Here you can:
     - Change Password: Update your password securely (encryption is applied automatically).
     - View Orders: See your order history and details.
     - Export Orders: Save your orders to a text file.
     - Logout: Return to the login screen.

4. **Admin Dashboard**
   - Admin users see the Admin Dashboard after login.
   - All events are listed with options to add, delete, modify, enable/disable, and view all orders.
   - The 'Account' menu at the top allows admin to:
     - Change Password
     - Logout (returns to login screen)
   - Admin can view all user orders, including customer usernames.

5. **Event Management**
   - Events are loaded from the database and can be managed by admin (add, delete, modify, enable/disable).
   - Duplicate event titles are not allowed.
   - Remaining seats are updated after each order.

6. **Order Management**
   - Users can view and export their own orders.
   - Admin can view all orders from all users.

7. **Password Security**
   - All passwords are encrypted using a Caesar cipher (shift by 3) before being stored in the database.
   - Login and password change operations always use encrypted values.

8. **General Notes**
   - The application uses a modern JavaFX GUI.
   - All order and event logic is fully database-backed (SQLite).
   - The application supports both normal users and admin roles.

If you have any issues, ensure you are running the application with JavaFX and SQLite JDBC dependencies available.

============================
Design Patterns Used
============================

1. **Model-View-Controller (MVC)**
   - The application is structured using the MVC pattern, separating the user interface (View), business logic (Model), and user input handling (Controller).

2. **Singleton Pattern**
   - The `Database` class is implemented as a Singleton. This ensures that only one instance of the database connection manager exists throughout the application, preventing resource leaks and ensuring consistent access to the SQLite database.
   - Usage: All DAOs and the Model class access the database connection via `Database.getInstance().getConnection()`.
   - Benefits: Centralizes database connection management, avoids multiple simultaneous connections, and simplifies resource cleanup.

3. **Event Creation Validation (Admin)**
   - When adding a new event, the 'Day' field must be one of: Mon, Tue, Wed, Thu, Fri, Sat, Sun.
   - You cannot add an event with the same title, day, and venue as an existing event.
   - If you enter an invalid day or try to add a duplicate event, a validation message will be shown.
