/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * RequiredMetadataDialog.java
 *
 * Created on Oct 11, 2010, 1:18:05 PM
 */
package org.irods.jargon.idrop.desktop.systraygui;

import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.irods.jargon.idrop.desktop.systraygui.services.PolicyService;
import org.irods.jargon.idrop.desktop.systraygui.viscomponents.PanelRequiredMetadataValue;
import org.irods.jargon.idrop.exceptions.IdropException;
import org.irods.jargon.idrop.exceptions.IdropRuntimeException;
import org.irods.jargon.part.policy.domain.Policy;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue;
import org.irods.jargon.part.policy.domain.PolicyRequiredMetadataValue.MetadataType;

/**
 * Dialog to process and present an interface to collect required metadata
 * @author mikeconway
 */
public class RequiredMetadataDialog extends javax.swing.JDialog {

    private String collectionAbsolutePath;
    private Policy policy;
    private iDrop idrop;

    /** Creates new form RequiredMetadataDialog */
    public RequiredMetadataDialog(final iDrop idrop, final String collectionAbsolutePath, final Policy policy, boolean modal) throws IdropException {
        super(idrop, modal);

        if (collectionAbsolutePath == null || collectionAbsolutePath.isEmpty()) {
            throw new IdropException("null or empth collectionAbsolutePath");
        }

        if (policy == null) {
            throw new IdropException("policy is null");
        }

        if (idrop == null) {
            throw new IdropException("idrop is null");
        }

        this.collectionAbsolutePath = collectionAbsolutePath;
        this.policy = policy;
        this.idrop = idrop;

        initComponents();
        buildMetadataPanels();
    }

    private void buildMetadataPanels() {

        for (PolicyRequiredMetadataValue requiredMetadataValue : policy.getPolicyRequiredMetadataValues()) {
            addPanelForMetadata(requiredMetadataValue);

        }


    }

    private void addPanelForMetadata(final PolicyRequiredMetadataValue policyRequiredMetadataValue)  {

        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {

                JPanel pnlRequiredMetadataValueEntry;
                try {
                    pnlRequiredMetadataValueEntry = new PanelRequiredMetadataValue(policyRequiredMetadataValue);
                } catch (IdropException ex) {
                    Logger.getLogger(RequiredMetadataDialog.class.getName()).log(Level.SEVERE, null, ex);
                    throw new IdropRuntimeException("runtime exception building metadata panel", ex);
                }
               
                pnlMetadataGrid.add(pnlRequiredMetadataValueEntry);
                pnlMetadataGrid.validate();
                scrollPaneMetadata.validate();
            }
        });

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPaneMetadata = new javax.swing.JScrollPane();
        pnlMetadataGrid = new javax.swing.JPanel();
        pnlBottom = new javax.swing.JPanel();
        btnCancel = new javax.swing.JButton();
        btnOK = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        scrollPaneMetadata.setMinimumSize(new java.awt.Dimension(300, 400));
        scrollPaneMetadata.setRowHeaderView(null);
        scrollPaneMetadata.setSize(new java.awt.Dimension(300, 400));

        pnlMetadataGrid.setMinimumSize(new java.awt.Dimension(200, 100));
        pnlMetadataGrid.setPreferredSize(new java.awt.Dimension(300, 300));
        pnlMetadataGrid.setSize(new java.awt.Dimension(300, 100));
        pnlMetadataGrid.setLayout(new java.awt.GridLayout(0, 1));
        scrollPaneMetadata.setViewportView(pnlMetadataGrid);

        getContentPane().add(scrollPaneMetadata, java.awt.BorderLayout.CENTER);

        pnlBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelActionPerformed(evt);
            }
        });
        pnlBottom.add(btnCancel);

        btnOK.setText("OK");
        btnOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnOKActionPerformed(evt);
            }
        });
        pnlBottom.add(btnOK);

        getContentPane().add(pnlBottom, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelActionPerformed
        this.dispose();
}//GEN-LAST:event_btnCancelActionPerformed

    private void btnOKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnOKActionPerformed
}//GEN-LAST:event_btnOKActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOK;
    private javax.swing.JPanel pnlBottom;
    private javax.swing.JPanel pnlMetadataGrid;
    private javax.swing.JScrollPane scrollPaneMetadata;
    // End of variables declaration//GEN-END:variables
}
