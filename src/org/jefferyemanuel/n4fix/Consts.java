/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jefferyemanuel.n4fix;

/**
 * This class holds global constants that are used throughout the application
 * to support in-app billing.
 */


public class Consts {
	
	// lock the class from being instantiated as this is only a place holder for constants
	private Consts() {

	}
	
	//developer and logging options
    public static final boolean DEVELOPER_MODE = false;
    public static final String TAG="n4fix";

    
    //admob items
    public static final String admob_publisherID = "a15196e9e0851e7";
    
    //wakelock constants
    public static final int WAKELOCK_ELAPSEDTIME=10000;
    
    //sensor constants
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int DETECTION_INTERVAL_SECONDS = 20;
    public static final int DETECTION_INTERVAL_MILLISECONDS =
            MILLISECONDS_PER_SECOND * DETECTION_INTERVAL_SECONDS;
    
    
}
