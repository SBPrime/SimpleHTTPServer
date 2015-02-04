/*
 * SimpleHTTPServer a plugin that allows you to run a simple HTTP server
 * directly from Spigot.
 * Copyright (c) 2015, SBPrime <https://github.com/SBPrime/>
 * Copyright (c) SimpleHTTPServer contributors
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted free of charge provided that the following 
 * conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer. 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution,
 * 3. Redistributions of source code, with or without modification, in any form 
 *    other then free of charge is not allowed,
 * 4. Redistributions in binary form in any form other then free of charge is 
 *    not allowed.
 * 5. Any derived work based on or containing parts of this software must reproduce 
 *    the above copyright notice, this list of conditions and the following 
 *    disclaimer in the documentation and/or other materials provided with the 
 *    derived work.
 * 6. The original author of the software is allowed to change the license 
 *    terms or the entire license of the software as he sees fit.
 * 7. The original author of the software is allowed to sublicense the software 
 *    or its parts using any license terms he sees fit.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.primesoft.simplehttpserver.services;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.primesoft.simplehttpserver.api.HttpStatusCodes;
import org.primesoft.simplehttpserver.api.IHeaders;
import org.primesoft.simplehttpserver.api.IHttpRequest;
import org.primesoft.simplehttpserver.api.IService;

/**
 *
 * @author SBPrime
 */
public class DefaultService implements IService {

    @Override
    public void handle(IHttpRequest request) throws IOException {
        int w = 256;
        int h = 256;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        int[] dataBuffInt = new int[w * h];

        int pos = 0;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                dataBuffInt[pos] = 0xff << 24 | // A
                        0x00 << 16 | // R
                        (i & 0xff) << 8 | // G
                        (j & 0xff); // B
                pos++;
            }
        }
        
        img.setRGB(0, 0, w, h, dataBuffInt, 0, w);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        baos.flush();
        byte[] imageInByte = baos.toByteArray();
        baos.close();

        IHeaders outHeaders = request.getResponseHeader();
        outHeaders.add("Content-Type", "image/png");

        request.sendResponse(HttpStatusCodes.OK, imageInByte.length);

        OutputStream os = request.getResponseBody();
        os.write(imageInByte);
        os.close();
    }

}
