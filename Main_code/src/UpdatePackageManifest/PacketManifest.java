/**
 *  PacketManifest data object holder class
 *
 *  The class to store the data the update manifest file have
 *  for later usage for verification of update package and sequence control of update difference
 *
 */
package UpdatePackageManifest;

public class PacketManifest {


    private String packageName,
           chPacket;

     // integers to hold  sequence number to track if update
     // Track of update are an increasing sequence number used to quick verify if present are older then publicised
    private Integer updateSeq;

    // Simple setters anf getters without error correction
    public void setPackageName(String pkgName) { packageName = pkgName; }
    public void setChPacket(String ch) { chPacket = ch; }

    public void setUpdateSeq(Integer seq) {
        updateSeq = seq; }

    public String getPackageName() { return packageName; }

    public String getChPacket() { return chPacket; }

    public Integer getUpdateSeq() { return updateSeq; }

}
