package pt.isec.pa.javalife.model.memento;

import java.util.ArrayDeque;
import java.util.Deque;

public class CareTaker {
    private final IOriginator originator;
    private final Deque<IMemento> history = new ArrayDeque<>();
    private final Deque<IMemento> redoHist = new ArrayDeque<>();

    public CareTaker(IOriginator originator) {
        this.originator = originator;
    }

    public void save() {
        redoHist.clear();
        history.push(originator.save());
    }

    public void undo() {
        if (history.isEmpty()) return;
        redoHist.push(originator.save());
        originator.restore(history.pop());
    }

}
