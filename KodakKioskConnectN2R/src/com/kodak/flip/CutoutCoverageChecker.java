package com.kodak.flip;



public class CutoutCoverageChecker
{

    private final static float SMALL_NUM_4_FLOAT_COMPARE = 1.0E-5F;
    
    public static boolean isCutoutFullyCovered(float cutoutWidth, float cutoutHeight, 
            float picUpperLeftX, float picUpperLeftY,
            float picUpperRightX, float picUpperRightY,
            float picLowerRightX, float picLowerRightY,
            float picLowerLeftX, float picLowerLeftY
            )
        {
            // Check cutout size argument basic value
            if (cutoutWidth <= 0.0 || cutoutHeight <= 0.0) {
                    return false;
            }

            // Re-arrange the vertex points, to get the current upper-left point, etc.
            float pULx, pULy, pURx, pURy, pLLx, pLLy, pLRx, pLRy;
            pULx = picUpperLeftX; pULy = picUpperLeftY;
            pURx = picUpperRightX; pURy = picUpperRightY;
            pLLx = picLowerLeftX; pLLy = picLowerLeftY;
            pLRx = picLowerRightX; pLRy = picLowerRightY;

            if ((picUpperRightX < pULx) ||
                (picUpperRightX - pULx <= SMALL_NUM_4_FLOAT_COMPARE && picUpperRightY <= pULy)
                ){
                pULx = picUpperRightX; pULy = picUpperRightY;

                pLLx = picUpperLeftX; pLLy = picUpperLeftY;
                pLRx = picLowerLeftX; pLRy = picLowerLeftY;
                pURx = picLowerRightX; pURy = picLowerRightY;
            }
            if ((picLowerLeftX < pULx) ||
                (picLowerLeftX - pULx <= SMALL_NUM_4_FLOAT_COMPARE && picLowerLeftY <= pULy)
                ) {
                pULx = picLowerLeftX; pULy = picLowerLeftY;

                pURx = picUpperLeftX; pURy = picUpperLeftY;
                pLRx = picUpperRightX; pLRy = picUpperRightY;
                pLLx = picLowerRightX; pLLy = picLowerRightY;
            }
            if ((picLowerRightX < pULx) ||
                (picLowerRightX - pULx <= SMALL_NUM_4_FLOAT_COMPARE && picLowerRightY <= pULy)
                ) {
                pULx = picLowerRightX; pULy = picLowerRightY;

                pLRx = picUpperLeftX; pLRy = picUpperLeftY;
                pLLx = picUpperRightX; pLLy = picUpperRightY;
                pURx = picLowerLeftX; pURy = picLowerLeftY;
            }

            //  check whether the picture cover the cutout without rotation or just 90/180 rotated.
            if (pLLx - pULx < SMALL_NUM_4_FLOAT_COMPARE && pURy - pULy < SMALL_NUM_4_FLOAT_COMPARE) {
                // If any vertex inside the cutout, then return false directly
                if (pURx < cutoutWidth || pLLy < cutoutHeight 
                    || pULx > SMALL_NUM_4_FLOAT_COMPARE || pULy > SMALL_NUM_4_FLOAT_COMPARE) {
                    return false;
                }
                return true;
            }
            // picture has rotation, basic vertex check first
            if (pURy >= SMALL_NUM_4_FLOAT_COMPARE // upper-right point must be above the cutout
                || pLLy <= cutoutHeight // lower-left point must be beneath the cutout
                || pLRx <= cutoutWidth  // lower-right point must be far than the cutout right edge
                || pULx >= SMALL_NUM_4_FLOAT_COMPARE  // lower-left point must not be the right side of left cutout edge
                ) {
                    return false;
            }

            // Checking the junction for upper-left vertex
            if (pULy > SMALL_NUM_4_FLOAT_COMPARE && pURx > SMALL_NUM_4_FLOAT_COMPARE) { // Need check the junction with cutout top line
                float tmpVal = (pURy < -SMALL_NUM_4_FLOAT_COMPARE) ? (-pURy) : pURy;
                float junctionLen = (pURx - pULx) * tmpVal / (pULy - pURy);
                if (junctionLen < pURx) return false; 
            }
            if (pULy < cutoutHeight && pLLx > SMALL_NUM_4_FLOAT_COMPARE) { // Need check the junction with cutout left line
                float tmpVal = (pULx < -SMALL_NUM_4_FLOAT_COMPARE) ? (-pULx) : pULx;
                float junctionLen = pULy + (pLLy - pULy) * tmpVal / (pLLx - pULx);
                if (junctionLen < cutoutHeight) return false; 
            }

            // Checking the junction for upper-right vertex
            if (pURx < cutoutWidth && pLRy > SMALL_NUM_4_FLOAT_COMPARE) { // Need check the junction with cutout right line
                float tmpVal = (pURy < -SMALL_NUM_4_FLOAT_COMPARE) ? (-pURy) : pURy;
                float junctionLen = (pLRx - pURx) * tmpVal / (pLRy - pURy) + pURx;
                if (junctionLen < cutoutWidth) return false; 
            }

            // Checking the junction for lower-left vertex
            if (pLLx < cutoutWidth && pLRy < cutoutHeight) { // Need check the junction with cutout bottom line
                float junctionLen = (pLLy - cutoutHeight) * (pLRx - pLLx) / (pLLy - pLRy) + pLLx;
                if (junctionLen < cutoutWidth) return false; 
            }

            return true;
        }
    
}
