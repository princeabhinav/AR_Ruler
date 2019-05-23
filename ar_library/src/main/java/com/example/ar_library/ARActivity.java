package com.example.ar_library;

import android.os.Bundle;

import android.util.Log;
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

public class ARActivity  {

    private CustomArFragment fragment;
    private ModelRenderable redSphereRenderable;

    private List<AnchorNode> anchorList = new ArrayList<>();
    private List<Node> viewRenderableList = new ArrayList<>();
    private String units = "in";


    private void onUpdateFrame(FrameTime frameTime){
        if (fragment.getArSceneView().getScene() == null){
            return;
        }

        Frame frame = fragment.getArSceneView().getArFrame();
        List<HitResult> hr = frame.hitTest(fragment.getView().getPivotX(), fragment.getView().getPivotY());



        for (Node node: viewRenderableList){
            updateOrientationTowardsCamera(node);
        }
    }


    private void calculateDistance(){
        if (anchorList.size() >= 2) {
            float distance = distanceBetweenAnchors(anchorList.get(anchorList.size() - 1).getAnchor(), anchorList.get(anchorList.size() - 2).getAnchor());
            Vector3 midPos = midPosBetweenAnchors(anchorList.get(anchorList.size() - 1).getAnchor(), anchorList.get(anchorList.size() - 2).getAnchor());

//            AnchorNode startNode = new AnchorNode(anchorList.get(anchorList.size() - 2));
//            AnchorNode endNode = new AnchorNode(anchorList.get(anchorList.size() - 1));
//            makeLineRenderable(startNode, endNode, distance, midPos);
        }
    }

    private float distanceBetweenAnchors(Anchor start, Anchor end){
        Pose startPose = start.getPose();
        Pose endPose = end.getPose();

        float dx = startPose.tx() - endPose.tx();
        float dy = startPose.ty() - endPose.ty();
        float dz = startPose.tz() - endPose.tz();

        // Compute the straight-line distance.
        float distanceMeters = (float) Math.sqrt(dx*dx + dy*dy + dz*dz);
        Log.e("distance cm", Float.toString(distanceMeters*100));
        return distanceMeters;
    }

    private Vector3 midPosBetweenAnchors(Anchor start, Anchor end){
        return Vector3.add(new AnchorNode(start).getWorldPosition(),
                new AnchorNode(end).getWorldPosition()).scaled(0.5f);
    }



    private void addDistanceText(ViewRenderable renderable, float distance, Vector3 worldPos){


        Node node = new Node();
        node.setRenderable(renderable);
        node.setWorldPosition(worldPos);
        fragment.getArSceneView().getScene().addChild(node);
        viewRenderableList.add(node);
    }



    private void addLine(ModelRenderable renderable, AnchorNode start, AnchorNode end, Vector3 worldPos){
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

    private void updateOrientationTowardsCamera(Node node){
        Vector3 cameraPosition = fragment.getArSceneView().getScene().getCamera().getWorldPosition();
        Vector3 cardPosition = node.getWorldPosition();
        Vector3 direction = Vector3.subtract(cameraPosition, cardPosition);
        Quaternion lookRotation = Quaternion.lookRotation(direction, Vector3.up());
        node.setWorldRotation(lookRotation);
    }

    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable){
        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(renderable);
        anchorList.add(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
    }

    static String cmToFeet(float centi)
    {
        int feet = (int) (0.0328 * centi);
        double inch = (0.3937 * centi) - 12 * feet ;
        if (feet == 0){
            return (String.format("%.2f", inch) + "in");
        }
        return (Integer.toString(feet) + "ft" + String.format("%.2f", inch) + "in");
    }

    private void clearAllNodes() {
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
}
