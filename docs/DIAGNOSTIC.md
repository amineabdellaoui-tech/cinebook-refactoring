# Diagnostic: smells found in `legacy/CinemaApp.java`

Line numbers refer to `legacy/CinemaApp.java` as committed in the first commit.

## 1. SOLID violations

| Principle | Evidence | Lines | Planned refactoring |
|---|---|---|---|
| **SRP** | `CinemaManager` handles catalogue, customers, pricing, payment, notifications, reports, search | 170-end | Split into `PricingService`, `PaymentService`, `BookingService`, `ReportService`, `SearchService` |
| **SRP** | `bookTicket`: ~150 lines, 8 parameters, validation + pricing + payment + persistence + notification | 270-418 | Extract Method, then extract classes |
| **OCP** | `if/else` on hall type, customer type, promo code, payment method | 329-392 | Strategy (pricing rules, promo, payment) |
| **OCP** | String-based `switch` in `runCommand` | 248-268 | Command |
| **LSP** | `ImaxShowtime.releaseSeat` throws `UnsupportedOperationException` | 107-111 | Move refund/cancel policy out of the hierarchy |
| **LSP** | `instanceof ImaxShowtime` in `cancelTicket` (client knows the subtype) | 441 | Polymorphic cancellation policy |
| **ISP** | `TicketOperations` forces `KioskMachine` to implement `cancel` / `refund` (they throw) | 139-169 | Role interfaces: `Bookable`, `Cancellable`, `Printable` |
| **DIP** | `new OldPaymentGateway()` inside business logic | 374 | `PaymentProcessor` interface + Adapter + constructor injection |
| **DIP** | `FileWriter`, `System.out` hard-wired in the manager | 565 and others | `ReportSink` / `Notifier` interfaces |

## 2. Coupling and cohesion (levels from the lecture)

| Kind | Evidence | Lines |
|---|---|---|
| Common coupling | `Globals.today`, `Globals.totalRevenue`, `Globals.ticketCounter` | 60-66 |
| Control coupling | `generateReport(String format, boolean detailed)`, `bookTicket(..., boolean popcorn, boolean drink, boolean glasses3d)` | 270, 521 |
| Stamp coupling | `Utils.sendEmail(Customer c, ...)` uses only the e-mail | 73 |
| Content coupling | `s.seats[row][col] = true`, `c.loyaltyPoints += ...` on public fields | 396, 475 |
| Coincidental cohesion | `Utils` (price format, e-mail, SMS, seat parsing) | 68-89 |
| Temporal cohesion | `init()` (config + seed data) | 176 |
| Logical cohesion | `search(String field, ...)` | 504 |

## 3. Design patterns we plan to introduce

| Pattern | Replaces |
|---|---|
| Strategy | Pricing by customer type / promo; payment method; report format |
| State | Ticket status as `String` (`BOOKED`, `USED`, `CANCELLED`) and its `if` chains (426-429, 471-479) |
| Chain of Responsibility | Booking validations (289-325) |
| Decorator | Extras (popcorn, drink, 3D glasses) added to the price (360-372) |
| Adapter | `OldPaymentGateway` (cents + integer codes) behind `PaymentProcessor` |
| Observer | Hard-coded e-mail + SMS (413-416) |
| Command | `runCommand` string dispatch (248-268) |
| Factory Method | `addShowtime` instantiating by hall type (221-225) |
| Template Method | `generateReport` structure shared by all formats |
| Facade | `CinemaFacade` as the single entry point for the UI/commands |
| Proxy (protection) | Repeated `role.equals("ADMIN")` checks (202, 216) |

## 4. Other smells
Public fields everywhere, magic numbers (14.0, 20.0, 0.8, 0.7, 4.5...), types and states stored as `String`, duplicated lookup loops (find customer / show / movie / ticket), very long methods, `instanceof`.

## 5. Safety net
`legacy/baseline_output.txt` is the recorded output of the original program.
`sh scripts/check_baseline.sh` must print `OK` after every refactoring step.
