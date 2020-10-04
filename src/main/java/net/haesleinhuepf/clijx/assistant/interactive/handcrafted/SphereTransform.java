package net.haesleinhuepf.clijx.assistant.interactive.handcrafted;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import net.haesleinhuepf.clijx.assistant.services.AssistantGUIPlugin;
import net.haesleinhuepf.clijx.assistant.AbstractAssistantGUIPlugin;
import net.haesleinhuepf.clij.clearcl.ClearCLBuffer;
import net.haesleinhuepf.spimcat.io.CLIJxVirtualStack;
import org.scijava.plugin.Plugin;

import java.awt.*;

@Plugin(type = AssistantGUIPlugin.class)
public class SphereTransform extends AbstractAssistantGUIPlugin {

    int number_of_angles = 360;
    float delta_angle_in_degrees = 1;
    float relative_center_x = 0.5f;
    float relative_center_y = 0.5f;
    float relative_center_z = 0.5f;


    private TextField center_x_slider;
    private TextField center_y_slider;
    private TextField center_z_slider;

    public SphereTransform() {
        super(new net.haesleinhuepf.clijx.plugins.SphereTransform());
    }

    protected boolean configure() {
        GenericDialog gdp = new GenericDialog("Sphere transform");
        //gdp.addImageChoice("Image", IJ.getImage().getTitle());
        gdp.addNumericField("Number of angles", number_of_angles);
        gdp.addNumericField("Angle step in degrees", delta_angle_in_degrees);
        gdp.showDialog();

        System.out.println("First dialog done");
        if (gdp.wasCanceled()) {
            System.out.println("First dialog cancelled");
            return false;
        }

        setSources(new ImagePlus[]{IJ.getImage()});
        number_of_angles = (int) gdp.getNextNumber();
        delta_angle_in_degrees = (float) gdp.getNextNumber();

        return true;
    }


    @Override
    protected GenericDialog buildNonModalDialog(Frame parent) {
        GenericDialog gdp = new GenericDialog("Sphere transform");
        gdp.addNumericField("Relative_center_x (0...1)", relative_center_x);
        addPlusMinusPanel(gdp, "relative_center_x");
        gdp.addNumericField("Relativce_center_y (0...1)", relative_center_y);
        addPlusMinusPanel(gdp, "relative_center_y");
        gdp.addNumericField("Relative_center_z (0...1)", relative_center_z);
        addPlusMinusPanel(gdp, "relative_center_z");

        center_x_slider = (TextField) gdp.getNumericFields().get(0);
        center_y_slider = (TextField) gdp.getNumericFields().get(1);
        center_z_slider = (TextField) gdp.getNumericFields().get(2);

        return gdp;
    }

    ClearCLBuffer[] result = null;
    public synchronized void refresh()
    {
        ClearCLBuffer[][] pushed = CLIJxVirtualStack.imagePlusesToBuffers(my_sources);

        if (center_y_slider != null) {
            relative_center_x = Float.parseFloat(center_x_slider.getText());
            relative_center_y = Float.parseFloat(center_y_slider.getText());
            relative_center_z = Float.parseFloat(center_z_slider.getText());
        }

        args = new Object[]{pushed[0], null, number_of_angles, delta_angle_in_degrees, relative_center_x * pushed[0][0].getWidth(), relative_center_y * pushed[0][0].getHeight(), relative_center_z * pushed[0][0].getDepth()};
        net.haesleinhuepf.clijx.plugins.SphereTransform plugin = (net.haesleinhuepf.clijx.plugins.SphereTransform) getCLIJMacroPlugin();
        plugin.setArgs(args);
        if (result == null) {
            result = createOutputBufferFromSource(pushed[0]);
        }
        args[1] = result[0];

        executeCL(pushed, new ClearCLBuffer[][]{result});
        cleanup(my_sources, pushed);

        setTarget(CLIJxVirtualStack.bufferToImagePlus(result));
        my_target.setTitle("Sphere transformed " + my_sources[0].getTitle());
        my_target.setDisplayRange(my_sources[0].getDisplayRangeMin(), my_sources[0].getDisplayRangeMax());
        my_target.updateAndDraw();
        enhanceContrast();

    }


    @Override
    public void refreshView() {}

}
