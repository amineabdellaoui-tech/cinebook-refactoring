import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CinemaApp {

    public static void main(String[] args) {
        CinemaManager cinema = new CinemaManager();
        cinema.init();

        System.out.println("=== Day 1: booking ===");
        Globals.today = 1;
        cinema.runCommand("BOOK C1 SH1 A1 CARD - PD3");
        cinema.runCommand("BOOK C2 SH2 B3 CASH PROMO10 -");
        cinema.runCommand("BOOK C3 SH4 C2 CASH - -");
        cinema.runCommand("BOOK C3 SH3 A1 CASH - -");
        cinema.runCommand("BOOK C2 SH3 A1 CASH FREE5 P");
        cinema.runCommand("BOOK C1 SH3 A1 CARD - -");
        cinema.runCommand("BOOK C1 SH3 Z9 CARD - -");
        cinema.runCommand("BOOK C4 SH2 A1 WALLET - -");
        cinema.runCommand("BOOK C5 SH2 A2 CARD - -");
        cinema.runCommand("BOOK C1 SH2 A3 CARD BADCODE -");

        System.out.println("=== Cancellations ===");
        cinema.runCommand("CANCEL T2");
        cinema.runCommand("CANCEL T1");

        System.out.println("=== Day 6: screening ===");
        Globals.today = 6;
        cinema.runCommand("USE T3");
        cinema.runCommand("USE T3");
        cinema.runCommand("CANCEL T3");

        System.out.println("=== Search ===");
        cinema.runCommand("SEARCH genre Sci");
        cinema.runCommand("SEARCH hall VIP");
        cinema.runCommand("SEARCH day 4");

        System.out.println("=== Kiosk ===");
        TicketOperations kiosk = new KioskMachine(cinema);
        kiosk.book("C2", "SH4", "D4");
        kiosk.print("T5");
        try {
            kiosk.refund("T5");
        } catch (UnsupportedOperationException e) {
            System.out.println("KIOSK ERROR: " + e.getMessage());
        }

        System.out.println("=== Reports ===");
        cinema.generateReport("TEXT", true);
        cinema.generateReport("CSV", false);
        cinema.generateReport("JSON", false);
        cinema.generateReport("FILE", true);
        System.out.println("Total revenue: " + Utils.formatPrice(Globals.totalRevenue));
    }
}

class Globals {
    static int today = 1;
    static int ticketCounter = 0;
    static double totalRevenue = 0;
    static boolean sendSms = true;
    static String reportPath = "cinema_report.txt";
}

class Utils {
    static String formatPrice(double p) {
        return String.format(Locale.US, "%.2f EUR", p);
    }

    static void sendEmail(Customer c, String msg) {
        System.out.println("   [EMAIL to " + c.email + "] " + msg);
    }

    static void sendSms(Customer c, String msg) {
        System.out.println("   [SMS to " + c.phone + "] " + msg);
    }

    static int seatRow(String seat) {
        return seat.charAt(0) - 'A';
    }

    static int seatCol(String seat) {
        return Integer.parseInt(seat.substring(1)) - 1;
    }
}

class Movie {
    public String id, title, genre;
    public int durationMin;
    public int ageRating;
}

class Showtime {
    public String id, movieId;
    public String hallType;
    public int day, hour;
    public boolean[][] seats = new boolean[5][8];

    public void releaseSeat(int row, int col) {
        seats[row][col] = false;
    }
}

class ImaxShowtime extends Showtime {
    @Override
    public void releaseSeat(int row, int col) {
        throw new UnsupportedOperationException("IMAX seats cannot be released");
    }
}

class Customer {
    public String id, name, email, phone, cardNumber;
    public String type;
    public int age;
    public int loyaltyPoints = 0;
    public double walletBalance = 0;
    public int failedPayments = 0;
    public boolean blocked = false;
}

class Ticket {
    public String id, customerId, showId, seat, extras, payMethod;
    public double price;
    public String status;
}

class OldPaymentGateway {
    public int makePayment(String cardNo, long amountInCents) {
        if (cardNo == null || cardNo.startsWith("0000")) {
            return 5;
        }
        return 0;
    }
}

interface TicketOperations {
    Ticket book(String customerId, String showId, String seat);
    void cancel(String ticketId);
    void refund(String ticketId);
    void print(String ticketId);
}

class KioskMachine implements TicketOperations {
    private final CinemaManager manager;

    KioskMachine(CinemaManager manager) {
        this.manager = manager;
    }

    public Ticket book(String customerId, String showId, String seat) {
        return manager.bookTicket(customerId, showId, seat, "CASH", "-", false, false, false);
    }

    public void cancel(String ticketId) {
        throw new UnsupportedOperationException("Kiosk cannot cancel tickets");
    }

    public void refund(String ticketId) {
        throw new UnsupportedOperationException("Kiosk cannot refund tickets");
    }

    public void print(String ticketId) {
        manager.printTicket(ticketId);
    }
}

class CinemaManager {
    public List<Movie> movies = new ArrayList<Movie>();
    public List<Showtime> shows = new ArrayList<Showtime>();
    public List<Customer> customers = new ArrayList<Customer>();
    public List<Ticket> tickets = new ArrayList<Ticket>();

    public void init() {
        System.out.println("Loading config...");
        Globals.today = 1;
        Globals.ticketCounter = 0;
        Globals.totalRevenue = 0;
        System.out.println("Seeding data...");

        addMovie("ADMIN", "MV1", "Interstellar", "Sci-Fi", 169, 12);
        addMovie("ADMIN", "MV2", "The Lion King", "Family", 118, 0);
        addMovie("ADMIN", "MV3", "Oppenheimer", "Drama", 180, 16);
        addMovie("ADMIN", "MV4", "Night Terror", "Horror", 95, 18);

        addShowtime("ADMIN", "SH1", "MV1", "IMAX", 3, 20);
        addShowtime("ADMIN", "SH2", "MV2", "STANDARD", 2, 15);
        addShowtime("ADMIN", "SH3", "MV3", "VIP", 6, 19);
        addShowtime("ADMIN", "SH4", "MV4", "STANDARD", 4, 22);

        addCustomer("C1", "Amine", "amine@um6p.ma", "0600000001", "STUDENT", 21, "4111111111111111", 0);
        addCustomer("C2", "Salma", "salma@um6p.ma", "0600000002", "MEMBER", 45, "4222222222222222", 0);
        addCustomer("C3", "Youssef", "youssef@mail.com", "0600000003", "REGULAR", 15, "4333333333333333", 0);
        addCustomer("C4", "Hajar", "hajar@mail.com", "0600000004", "SENIOR", 70, "4444444444444444", 5.0);
        addCustomer("C5", "Omar", "omar@mail.com", "0600000005", "REGULAR", 30, "0000555555555555", 0);
        System.out.println("Ready.");
    }

    public void addMovie(String role, String id, String title, String genre, int duration, int ageRating) {
        if (!role.equals("ADMIN")) {
            System.out.println("ERROR: access denied");
            return;
        }
        Movie m = new Movie();
        m.id = id;
        m.title = title;
        m.genre = genre;
        m.durationMin = duration;
        m.ageRating = ageRating;
        movies.add(m);
    }

    public void addShowtime(String role, String id, String movieId, String hallType, int day, int hour) {
        if (!role.equals("ADMIN")) {
            System.out.println("ERROR: access denied");
            return;
        }
        Showtime s;
        if (hallType.equals("IMAX")) {
            s = new ImaxShowtime();
        } else {
            s = new Showtime();
        }
        s.id = id;
        s.movieId = movieId;
        s.hallType = hallType;
        s.day = day;
        s.hour = hour;
        shows.add(s);
    }

    public void addCustomer(String id, String name, String email, String phone, String type, int age,
                            String card, double wallet) {
        Customer c = new Customer();
        c.id = id;
        c.name = name;
        c.email = email;
        c.phone = phone;
        c.type = type;
        c.age = age;
        c.cardNumber = card;
        c.walletBalance = wallet;
        customers.add(c);
    }

    public void runCommand(String line) {
        System.out.println("> " + line);
        String[] p = line.split(" ");
        switch (p[0]) {
            case "BOOK":
                bookTicket(p[1], p[2], p[3], p[4], p[5],
                        p[6].contains("P"), p[6].contains("D"), p[6].contains("3"));
                break;
            case "CANCEL":
                cancelTicket(p[1]);
                break;
            case "USE":
                useTicket(p[1]);
                break;
            case "SEARCH":
                search(p[1], p[2]);
                break;
            default:
                System.out.println("ERROR: unknown command " + p[0]);
        }
    }

    public Ticket bookTicket(String customerId, String showId, String seat, String payMethod,
                             String promo, boolean popcorn, boolean drink, boolean glasses3d) {
        Customer c = null;
        for (Customer x : customers) {
            if (x.id.equals(customerId)) c = x;
        }
        Showtime s = null;
        for (Showtime y : shows) {
            if (y.id.equals(showId)) s = y;
        }
        if (c == null || s == null) {
            System.out.println("ERROR: unknown customer or showtime");
            return null;
        }
        Movie m = null;
        for (Movie z : movies) {
            if (z.id.equals(s.movieId)) m = z;
        }

        if (c.blocked) {
            System.out.println("ERROR: " + c.name + " is blocked");
            return null;
        }
        if (c.age < m.ageRating) {
            System.out.println("ERROR: " + c.name + " is too young for " + m.title + " (" + m.ageRating + "+)");
            return null;
        }
        if (seat.length() < 2 || seat.charAt(0) < 'A' || seat.charAt(0) > 'E') {
            System.out.println("ERROR: invalid seat " + seat);
            return null;
        }
        int row = seat.charAt(0) - 'A';
        int col;
        try {
            col = Integer.parseInt(seat.substring(1)) - 1;
        } catch (NumberFormatException e) {
            System.out.println("ERROR: invalid seat " + seat);
            return null;
        }
        if (col < 0 || col > 7) {
            System.out.println("ERROR: invalid seat " + seat);
            return null;
        }
        if (s.seats[row][col]) {
            System.out.println("ERROR: seat " + seat + " is already taken");
            return null;
        }
        int already = 0;
        for (Ticket t : tickets) {
            if (t.customerId.equals(customerId) && t.showId.equals(showId) && t.status.equals("BOOKED")) {
                already++;
            }
        }
        if (already >= 4) {
            System.out.println("ERROR: max 4 tickets per show");
            return null;
        }

        double price;
        if (s.hallType.equals("IMAX")) {
            price = 14.0;
        } else if (s.hallType.equals("VIP")) {
            price = 20.0;
        } else {
            price = 8.0;
        }
        if (s.day % 7 == 6 || s.day % 7 == 0) {
            price += 2.0;
        }
        if (s.hour >= 18) {
            price += 1.5;
        }
        if (c.type.equals("STUDENT")) {
            price = price * 0.8;
        } else if (c.type.equals("SENIOR")) {
            price = price * 0.7;
        } else if (c.type.equals("MEMBER")) {
            price = price * 0.9;
        }
        if (promo != null && !promo.equals("-")) {
            if (promo.equals("PROMO10")) {
                price = price * 0.9;
            } else if (promo.equals("FREE5")) {
                price = price - 5.0;
                if (price < 0) price = 0;
            } else {
                System.out.println("WARNING: invalid promo code " + promo);
            }
        }
        String extras = "";
        if (popcorn) {
            price += 4.5;
            extras += "popcorn ";
        }
        if (drink) {
            price += 3.0;
            extras += "drink ";
        }
        if (glasses3d) {
            price += 2.0;
            extras += "3D-glasses ";
        }

        if (payMethod.equals("CARD")) {
            OldPaymentGateway gw = new OldPaymentGateway();
            int code = gw.makePayment(c.cardNumber, Math.round(price * 100));
            if (code != 0) {
                System.out.println("ERROR: card declined (code " + code + ")");
                c.failedPayments++;
                if (c.failedPayments >= 3) {
                    c.blocked = true;
                }
                return null;
            }
        } else if (payMethod.equals("CASH")) {
        } else if (payMethod.equals("WALLET")) {
            if (c.walletBalance < price) {
                System.out.println("ERROR: insufficient wallet balance for " + Utils.formatPrice(price));
                return null;
            }
            c.walletBalance -= price;
        } else {
            System.out.println("ERROR: unknown payment method " + payMethod);
            return null;
        }

        s.seats[row][col] = true;
        Ticket t = new Ticket();
        Globals.ticketCounter++;
        t.id = "T" + Globals.ticketCounter;
        t.customerId = customerId;
        t.showId = showId;
        t.seat = seat;
        t.price = price;
        t.payMethod = payMethod;
        t.extras = extras.trim();
        t.status = "BOOKED";
        tickets.add(t);
        Globals.totalRevenue += price;

        System.out.println("OK: " + c.name + " booked " + m.title + " seat " + seat + " (" + s.hallType
                + ", day " + s.day + " " + s.hour + "h) for " + Utils.formatPrice(price)
                + (extras.isEmpty() ? "" : " with " + extras.trim()) + " -> " + t.id);
        Utils.sendEmail(c, "Ticket " + t.id + " confirmed");
        if (Globals.sendSms) {
            Utils.sendSms(c, "Ticket " + t.id + " ok");
        }
        return t;
    }

    public void cancelTicket(String ticketId) {
        Ticket t = findTicket(ticketId);
        if (t == null) {
            System.out.println("ERROR: unknown ticket");
            return;
        }
        if (t.status.equals("CANCELLED")) {
            System.out.println("ERROR: ticket already cancelled");
            return;
        } else if (t.status.equals("USED")) {
            System.out.println("ERROR: a used ticket cannot be cancelled");
            return;
        }
        Showtime s = null;
        for (Showtime y : shows) {
            if (y.id.equals(t.showId)) s = y;
        }
        Customer c = null;
        for (Customer x : customers) {
            if (x.id.equals(t.customerId)) c = x;
        }
        if (s instanceof ImaxShowtime) {
            System.out.println("ERROR: IMAX tickets are non-refundable");
            return;
        }
        int daysLeft = s.day - Globals.today;
        double pct;
        if (daysLeft >= 2) {
            pct = 1.0;
        } else if (daysLeft >= 1) {
            pct = 0.5;
        } else {
            pct = 0.0;
        }
        double refund = t.price * pct;
        s.releaseSeat(Utils.seatRow(t.seat), Utils.seatCol(t.seat));
        t.status = "CANCELLED";
        Globals.totalRevenue -= refund;
        if (t.payMethod.equals("WALLET")) {
            c.walletBalance += refund;
        }
        System.out.println("OK: " + t.id + " cancelled, refund " + Utils.formatPrice(refund));
        Utils.sendEmail(c, "Ticket " + t.id + " cancelled, refund " + Utils.formatPrice(refund));
    }

    public void useTicket(String ticketId) {
        Ticket t = findTicket(ticketId);
        if (t == null) {
            System.out.println("ERROR: unknown ticket");
            return;
        }
        if (t.status.equals("BOOKED")) {
            t.status = "USED";
            for (Customer c : customers) {
                if (c.id.equals(t.customerId)) {
                    c.loyaltyPoints += (int) t.price;
                    System.out.println("OK: " + t.id + " used, " + c.name + " now has " + c.loyaltyPoints + " points");
                }
            }
        } else if (t.status.equals("USED")) {
            System.out.println("ERROR: ticket already used");
        } else {
            System.out.println("ERROR: ticket is " + t.status);
        }
    }

    public void printTicket(String ticketId) {
        Ticket t = findTicket(ticketId);
        if (t == null) {
            System.out.println("ERROR: unknown ticket");
            return;
        }
        System.out.println("   ---- TICKET " + t.id + " ----");
        System.out.println("   show " + t.showId + " seat " + t.seat + " price " + Utils.formatPrice(t.price));
        System.out.println("   status " + t.status);
    }

    private Ticket findTicket(String id) {
        for (Ticket t : tickets) {
            if (t.id.equals(id)) return t;
        }
        return null;
    }

    public void search(String field, String value) {
        System.out.println("Search " + field + "=" + value + ":");
        for (Showtime s : shows) {
            Movie m = null;
            for (Movie z : movies) {
                if (z.id.equals(s.movieId)) m = z;
            }
            if (field.equals("genre") && m.genre.toLowerCase().contains(value.toLowerCase())) {
                System.out.println("   " + s.id + " " + m.title + " (" + s.hallType + ")");
            } else if (field.equals("hall") && s.hallType.equals(value)) {
                System.out.println("   " + s.id + " " + m.title + " (" + s.hallType + ")");
            } else if (field.equals("day") && s.day == Integer.parseInt(value)) {
                System.out.println("   " + s.id + " " + m.title + " (" + s.hallType + ")");
            }
        }
    }

    public void generateReport(String format, boolean detailed) {
        StringBuilder sb = new StringBuilder();
        if (format.equals("TEXT") || format.equals("FILE")) {
            sb.append("CINEMA REPORT (day " + Globals.today + ")\n");
            for (Showtime s : shows) {
                int taken = 0;
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 8; j++) {
                        if (s.seats[i][j]) taken++;
                    }
                }
                sb.append(s.id + " | " + s.hallType + " | day " + s.day + " | " + taken + "/40 seats\n");
            }
            if (detailed) {
                for (Ticket t : tickets) {
                    sb.append(t.id + " " + t.customerId + " " + t.showId + " " + t.seat + " "
                            + Utils.formatPrice(t.price) + " " + t.status + "\n");
                }
                for (Customer c : customers) {
                    sb.append(c.name + " (" + c.type + ") points=" + c.loyaltyPoints
                            + " wallet=" + Utils.formatPrice(c.walletBalance) + "\n");
                }
            }
        } else if (format.equals("CSV")) {
            sb.append("ticket;customer;show;seat;price;status\n");
            for (Ticket t : tickets) {
                sb.append(t.id + ";" + t.customerId + ";" + t.showId + ";" + t.seat + ";"
                        + String.format(Locale.US, "%.2f", t.price) + ";" + t.status + "\n");
            }
        } else if (format.equals("JSON")) {
            sb.append("[\n");
            for (int i = 0; i < tickets.size(); i++) {
                Ticket t = tickets.get(i);
                sb.append("  {\"id\":\"" + t.id + "\",\"seat\":\"" + t.seat + "\",\"price\":"
                        + String.format(Locale.US, "%.2f", t.price) + ",\"status\":\"" + t.status + "\"}");
                sb.append(i < tickets.size() - 1 ? ",\n" : "\n");
            }
            sb.append("]\n");
        } else {
            throw new IllegalArgumentException("Unknown format " + format);
        }

        if (format.equals("FILE")) {
            try {
                FileWriter fw = new FileWriter(Globals.reportPath);
                fw.write(sb.toString());
                fw.close();
                System.out.println("Report written to " + Globals.reportPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.print(sb.toString());
        }
    }
}
