package hellonaga.teavm;

import hellonaga.HelloNagaLogic;
import naga.core.spi.plat.teavm.TeaVmPlatform;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;

/**
 * @author Bruno Salmon
 */
public class HelloNagaTeaVmApplication {

    static {
        TeaVmPlatform.register();
    }

    public static void main(String[] args) {
        new HelloNagaLogic(HelloNagaTeaVmApplication::displayMessage).run();
    }

    private static void displayMessage(String helloMessage) {
        // Tracing the message in the console
        System.out.println(helloMessage);

        // Displaying the message in the DOM
        HTMLDocument document = HTMLDocument.current();
        HTMLElement p = document.createElement("p");
        p.appendChild(document.createTextNode(helloMessage));
        document.getBody().appendChild(p);
    }

}
