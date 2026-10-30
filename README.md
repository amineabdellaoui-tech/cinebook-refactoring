# CineBook: refactoring project (Software Engineering)

**Course:** Software Engineering, UM6P College of Computing
**Team:** _Name 1_ and _Name 2_

CineBook is a console-based **cinema ticket booking system** (movies, showtimes, customers,
tickets, pricing, payments, notifications, reports). It works, but its design has decayed.
This repository contains the **legacy code** we propose to refactor, a **diagnostic** of its problems,
and our **refactoring plan**.

## Run it

Requires Java 17+ (tested with Java 21).

```bash
java legacy/CinemaApp.java
```

## Safety net (characterization test)

```bash
sh scripts/check_baseline.sh      # must print: OK: output identical to baseline
```

The expected output of the original program is stored in `legacy/baseline_output.txt`.
After every refactoring step the behaviour must stay identical.

## What is wrong with it (summary)

Full details with line numbers: [`docs/DIAGNOSTIC.md`](docs/DIAGNOSTIC.md)

- **SOLID:** a God class (SRP); `if/else` chains on types, promos and payments (OCP);
  `ImaxShowtime` throwing `UnsupportedOperationException` and an `instanceof` check (LSP);
  a fat `TicketOperations` interface (ISP); `new OldPaymentGateway()` inside business logic (DIP).
- **Coupling / cohesion:** global mutable state (common), flag parameters (control),
  whole objects passed to use one field (stamp), direct writes to public fields (content),
  a `Utils` grab bag (coincidental cohesion).
- **Smells:** a 150-line `bookTicket` with 8 parameters, magic numbers, duplicated lookups, Strings used as types and states.

## Refactoring plan

| Step | Goal | Techniques |
|---|---|---|
| 0 | Safety net | baseline output + `check_baseline.sh` (then JUnit tests) |
| 1 | Clean up | Extract Method, constants, encapsulation, remove duplication |
| 2 | SOLID | Split `CinemaManager` (SRP), interfaces for pricing/payment (OCP, DIP), fix the IMAX hierarchy (LSP), role interfaces (ISP) |
| 3 | Design patterns | Strategy, State, Chain of Responsibility, Decorator, Adapter, Observer, Command, Factory Method, Template Method, Facade |
| 4 | Project structure | Maven layout, packages, JUnit tests, before/after UML class diagrams |

## Repository layout

```
legacy/      original code + recorded baseline output
docs/        diagnostic (smells, principles, patterns, line numbers)
scripts/     characterization test
```

The refactored version will be added on a separate branch (`refactoring`) and compared with this one.
