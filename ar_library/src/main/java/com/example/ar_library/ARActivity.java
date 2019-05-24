package com.example.ar_library;

import android.content.Context;
import android.os.Bundle;

import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Pose;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.List;

public class ARActivity {

    public CustomArFragment fragment;
    public ModelRenderable redSphereRenderable;

    //these nodes stay positioned in the same place relative to the real world
    public List<AnchorNode> anchorList = new ArrayList<>();

    //these are based on the base type node but can be changed in terms of position scaling and rotation
    public List<Node> viewRenderableList = new ArrayList<>();
    public String units = "in";

    //this function takes time information for the current frame and then stores the co-ordinates of frame into a list.
   /* private void onUpdateFrame(FrameTime frameTime){
        if (fragment.getArSceneView().getScene() == null){
            return;
        }

        Frame frame = fragment.getArSceneView().getArFrame();
        List<HitResult> hr = frame.hitTest(fragment.getView().getPivotX(), fragment.getView().getPivotY());

        if (hr.isEmpty()){
            updateTargetColor(android.graphics.Color.RED);
        }
        else {
            updateTargetColor(android.graphics.Color.GREEN);
        }

        for (Node node: viewRenderableList){
            updateOrientationTowardsCamera(node);
        }
    }*/

    //checks if the size of the anchorlist is greater than 2 then it call the distanceBetweenAnchors function and midpos function, also passes the value of one node from the list at a time.
    public void calculateDistance(){
        if (anchorList.size() >= 2) {
            float distance = distanceBetweenAnchors(anchorList.get(anchorList.size() - 1).getAnchor(), anchorList.get(anchorList.size() - 2).getAnchor());
            Vector3 midPos = midPosBetweenAnchors(anchorList.get(anchorList.size() - 1).getAnchor(), anchorList.get(anchorList.size() - 2).getAnchor());

//            AnchorNode startNode = new AnchorNode(anchorList.get(anchorList.size() - 2));
//            AnchorNode endNode = new AnchorNode(anchorList.get(anchorList.size() - 1));
//            makeLineRenderable(startNode, endNode, distance, midPos);
        }
    }
    //takes the value of the anchor node and find the distance between them using distance formula.
    public float distanceBetweenAnchors(Anchor start, Anchor end){
        Pose startPose = start.getPose();
        Pose endPose = end.getPose();

        float dx = startPose.tx() - endPose.tx();
        float dy = startPose.ty() - endPose.ty();
        float dz = startPose.tz() - endPose.tz();

        //Compute the straight-line distance.
        float distanceMeters = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        Log.e("distance cm", Float.toString(distanceMeters*100));
        return distanceMeters;
    }
    //taking parameter as Anchor nodes and finding the mid position between them and then returning the mid-position.
    public Vector3 midPosBetweenAnchors(Anchor start, Anchor end){
        return Vector3.add(new AnchorNode(start).getWorldPosition(),
                new AnchorNode(end).getWorldPosition()).scaled(0.5f);
    }



   /* private void addDistanceText(ViewRenderable renderable, float distance, Vector3 worldPos){
        TextView txt = (TextView) renderable.getView().findViewById(R.id.planetInfoCard);
        if (units.equals("cm")) {
            txt.setText(String.format("%.2f", distance));
        }
        else{
            txt.setText(cmToFeet(distance));
        }
        Node node = new Node();
        node.setRenderable(renderable);
        node.setWorldPosition(worldPos);
        fragment.getArSceneView().getScene().addChild(node);
        viewRenderableList.add(node);
    }*/


    //
    public void addLine(ModelRenderable renderable, AnchorNode start, AnchorNode end, Vector3 worldPos){
        Node node = new Node();
        node.setRenderable(renderable);
        node.setWorldPosition(worldPos);
        Vector3 vector = Vector3.subtract(end.getWorldPosition(), start.getWorldPosition());
        Quaternion lookRotation = Quaternion.lookRotation(vector, Vector3.up());
        // Rotate 90° along the right vector (1, 0, 0)
        Quaternion worldRotation = Quaternion.multiply(lookRotation, Quaternion.axisAngle(Vector3.right(), 90));
        node.setWorldRotation(worldRotation);
        fragment.getArSceneView().getScene().addChild(node);
    }

    //taking the changable node as parameter and then
    public void updateOrientationTowardsCamera(Node node){
        Vector3 cameraPosition = fragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = node.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        node.setWorldRotation(lookRotation);
    }
    //??
    public void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable){
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(renderable);
        anchorList.add(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
    }
    //this function takes values in cm and convert them into feet and return the changed values in feet if feet value is 0 it returns result into inches.
    public static String cmToFeet(float centi)
    {
        int feet = (int) (0.0328 * centi);
        double inch = (0.3937 * centi) - 12 * feet ;
        if (feet == 0){
            return (String.format("%.2f", inch) + "in");
        }
        return (Integer.toString(feet) + "ft" + String.format("%.2f", inch) + "in");
    }
    //this function removes all the nodes which are the object of AnchorNodes and make a new viewRenderable list and AnchorNode list.
    public void clearAllNodes() {
        List<Node> children = new ArrayList<>(fragment.getArSceneView().getScene().getChildren());
        for (Node node : children) {
            if (node instanceof AnchorNode) {
                fragment.getArSceneView().getScene().removeChild(node);
                ((AnchorNode) node).getAnchor().detach();
            }

            for (Node textView: viewRenderableList){
                textView.setParent(null);
            }
            viewRenderableList = new ArrayList<>();
            anchorList = new ArrayList<>();
        }
    }
    //just for checking
    public static void s(Context c, String message)
    {
        Toast.makeText(c,message,Toast.LENGTH_SHORT).show();
    }
}
