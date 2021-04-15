package commanders;

import java.util.concurrent.Flow;

public interface Commander extends Flow.Publisher<Command> {
}
