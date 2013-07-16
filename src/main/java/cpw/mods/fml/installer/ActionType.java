package cpw.mods.fml.installer;

import java.io.File;
import java.util.List;

public interface ActionType {
    boolean run(File target, List<ActionModifier> modifiers);
    boolean isPathValid(File targetDir);
    String getFileError(File targetDir);
    String getSuccessMessage();
}
