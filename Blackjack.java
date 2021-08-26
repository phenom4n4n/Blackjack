import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.ListIterator;
import java.util.Scanner;

public class Blackjack {
    public static void main(String[] args) {
        BlackjackGame game = new BlackjackGame();
        game.start();
    }
}

class BlackjackGame {
    private ArrayList<Card> cards;
    private Dealer dealer;
    private User user;
    public Scanner scanner;
    private boolean gameEnded;
    public static final String splitter = "━━━━━━━━━━━━━━━━━━♡♤♡━━━━━━━━━━━━━━━━━━";
    private boolean _holeCardRevealed;

    public BlackjackGame() {
        cards = new ArrayList<Card>(52);
        String[] suits = {"Clubs", "Diamonds", "Hearts", "Spades"};
        for (String suit : suits) {
            generateCardSuit(suit, cards);
        }
        dealer = new Dealer(this);
        user = new User(this);
        scanner = new Scanner(System.in);
        gameEnded = false;
    }

    private static void generateCardSuit(String suit, ArrayList<Card> cards) {
        for (int i = 2; i < 11; i++) {
            cards.add(new Card(suit, "", i));
        }
        String[] royals = {"Jack", "Queen", "King"};
        for (String name : royals) {
            cards.add(new Card(suit, name, 10));
        }
        cards.add(Card.ace(suit));
    }

    public void start() {
        println("Shuffling cards..", 0.5);
        Collections.shuffle(cards);
        println("Dealing cards..", 0.5);
        deal(user);
        deal(dealer);
        deal(user);
        deal(dealer);
        try {
            playTurn();            
        } catch (UserBustedException exc) {
            println(exc.getMessage(), 1);
        }
    }

    public void deal(Player player) {
        Card card = cards.remove(0);
        player.deal(card);
    }

    private void validateCards() throws UserBustedException {
        int value = user.getCardsValue();
        if (value > 21) {
            throw new UserBustedException(user, value);
        }
    }

    private void playTurn() throws UserBustedException {
        validateCards();
        while (user.canPlay()) {
            user.playTurn();
            validateCards();
        }
        dealer.playTurn();
        int userValue = user.getCardsValue();
        int dealerValue = dealer.getCardsValue();
        if (userValue == dealerValue) {
            println("You tied with the dealer.", 0.5);
        } else if (userValue > dealerValue) {
            println("You won!", 0.5);
        } else {
            println("The dealer won :(", 0.5);
        }
        println("Dealer card value: " + dealerValue, 0.25);
        println("User card value: " + userValue, 0.25);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(BlackjackGame.splitter)
            .append("\nCards left: ")
            .append(cards.size())
            .append("\n")
            .append(dealer.toString())
            .append("\n\n")
            .append(user.toString())
            .append("\n")
            .append(BlackjackGame.splitter);
        return stringBuilder.toString();
    }

    public void revealHoleCard() {
        _holeCardRevealed = true;
        println("The dealer reveals the hole card!", 0.5);
        println(dealer.toString(), 0.5);
    }

    public boolean holeCardRevealed() {
        return _holeCardRevealed;
    }

    public void print() {
        println(toString(), 0.5);
    }

    public static void println(Object message, double sleepTime) {
        println(message);
        sleepTime *= 1000;
        long millis = (long) sleepTime;
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            println(exception);
        }
    }

    public static void println(Object message) {
        System.out.println(message);
    }
}

class Card {
    public static final String leftBracket = "〚";
    public static final String rightBracket = "〛";
    public String suit;
    public String name;
    private int value;

    public Card(String suit, String name, int value) {
        this.suit = suit;
        this.name = name;
        this.value = value;
    }

    public static Card ace(String suit) {
        return new Card(suit, "Ace", 0);
    }

    public int getValue() {
        if (value == 0) {
            // ace logic
            return 1;
        } else {
            return value;
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Card(");
        stringBuilder.append(suit);
        if (name.isEmpty() != true) {
            stringBuilder.append(", ").append(name);
        }
        stringBuilder.append(", ").append(value).append(")");
        return stringBuilder.toString();
    }

    public String toPrettyString() {
        StringBuilder stringBuilder = new StringBuilder(leftBracket);
        if (name.isEmpty() == true) {
            stringBuilder.append(value);
        } else {
            stringBuilder.append(name);
        }
        return stringBuilder.append(" of ").append(suit).append(rightBracket).toString();
    }
}

class Player {
    public ArrayList<Card> cards;
    public BlackjackGame game;

    public Player(BlackjackGame blackjackGame) {
        cards = new ArrayList<Card>();
        game = blackjackGame;
    }

    public int getCardsValue() {
        int value = 0;
        for (Card card : cards) {
            value += card.getValue();
        }
        return value;
    }

    public void deal(Card card) {
        cards.add(card);
    }

    public void sortCards() {
        Comparator comparator = new Comparator<Card>(){
            public int compare(Card card1, Card card2) {
                int card1Value = card1.getValue();
                int card2Value = card2.getValue();
                if (card1Value > card2Value) {
                    return -1;
                } else if (card1Value < card2Value) {
                    return 1;
                } else {
                    return card1.name.compareTo(card2.name);
                }
            }
        };
        Collections.sort(cards, comparator);
    }
}

class Dealer extends Player {
    public Dealer(BlackjackGame blackjackGame) {
        super(blackjackGame);
    }

    public Card holeCard() {
        return cards.get(0);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Dealer's cards: ")
            .append(cards.size())
            .append("\n");
        int index;
        if (game.holeCardRevealed() != true) {
            stringBuilder.append(Card.leftBracket)
                .append("Face Down")
                .append(Card.rightBracket)
                .append(" ");
            index = 1;
        } else {
            index = 0;
        }
        ListIterator<Card> cardsList = cards.listIterator(index);
        while (cardsList.hasNext()) {
            Card card = cardsList.next();
            stringBuilder.append(card.toPrettyString()).append(" ");
        }
        return stringBuilder.toString();
    }

    public void playTurn() {
        if (game.holeCardRevealed() != true) {
            game.revealHoleCard();
        }
        while (getCardsValue() < 17) {
            game.deal(this);
            game.print();
        }
    }
}

class User extends Player {
    private boolean stood;
    private boolean busted;

    public User(BlackjackGame blackjackGame) {
        super(blackjackGame);
        stood = false;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("Your cards:\n");
        sortCards();
        for (Card card : cards) {
            stringBuilder.append(card.toPrettyString()).append(" ");
        }
        return stringBuilder.toString();
    }

    public boolean hasStood() {
        return stood;
    }

    public void setStood(boolean value) {
        stood = value;
    }

    public boolean hasBusted() {
        return busted;
    }

    public void setBusted(boolean value) {
        busted = value;
    }

    public boolean canPlay() {
        if (hasStood() || hasBusted()) {
            return false;
        } else {
            return true;
        }
    }

    public void playTurn() {
        game.print();
        BlackjackGame.println("Will you hit (h) or stand (s)?");
        String choice = game.scanner.nextLine().toLowerCase();
        if (choice.equals("h")) {
            game.deal(this);
        } else if (choice.equals("s")) {
           setStood(true);
        } else {
            BlackjackGame.println("Invalid choice.", 0.25);
            playTurn();
        }
    }
}

class UserBustedException extends Exception {
    String message;

    public UserBustedException(User user, int cardsValue) {
        super("You busted! (" + cardsValue + ")");
        user.setBusted(true);
    }
}
