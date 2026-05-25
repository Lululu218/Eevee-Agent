/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.azheng.boot.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class StartupBannerConfig implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) {
        printStartupBanner();
    }

    private void printStartupBanner() {
        // ANSI 绿色：\033[32m | 重置颜色：\033[0m
        String banner = "\n" +
                "                                      ##                                                          \n" +
                "                          ###         ###              ###       ###                              \n" +
                "                          #######    ##############    #####  ####### #########            $$$    \n" +
                "                             ###### ################    #### #######  ##########           $$$    \n" +
                "                               #### ####  ##   ####          #####    ####  ####           $$$    \n" +
                "                        ####  ####  ###  ####  ####    ###### ####    ####  ####           $$$    \n" +
                "                          ########  ### ##### ####      ##### ####    ####  ####           $$$    \n" +
                "                           ######      #####   ###       #### #####   ####  ####           $$$    \n" +
                "                           #####      #####              ####  #####  #### #####                  \n" +
                "                           ######    ########            ####  ###### #### #####           $$$    \n" +
                "                          #### #### ##### #####          #####  ##### ####                 $$$    \n" +
                "                         ####    # #####    #####      ########       ####                        \n" +
                "                         ####      ####        ###    #########################                   \n" +
                "                                                                    \n" +
                "_______   _______   ___      ___ _______   _______           ________  ________  _______   ________   _________   \n" +
                "|\\  ___ \\ |\\  ___ \\ |\\  \\    /  /|\\  ___ \\ |\\  ___ \\         |\\   __  \\|\\   ____\\|\\  ___ \\ |\\   ___  \\|\\___   ___\\ \n" +
                "\\ \\   __/|\\ \\   __/|\\ \\  \\  /  / | \\   __/|\\ \\   __/|        \\ \\  \\|\\  \\ \\  \\___|\\ \\   __/|\\ \\  \\\\ \\  \\|___ \\  \\_| \n" +
                " \\ \\  \\_|/_\\ \\  \\_|/_\\ \\  \\/  / / \\ \\  \\_|/_\\ \\  \\_|/__       \\ \\   __  \\ \\  \\  __\\ \\  \\_|/_\\ \\  \\\\ \\  \\   \\ \\  \\  \n" +
                "  \\ \\  \\_|\\ \\ \\  \\_|\\ \\ \\    / /   \\ \\  \\_|\\ \\ \\  \\_|\\ \\       \\ \\  \\ \\  \\ \\  \\|\\  \\ \\  \\_|\\ \\ \\  \\\\ \\  \\   \\ \\  \\ \n" +
                "   \\ \\_______\\ \\_______\\ \\__/ /     \\ \\_______\\ \\_______\\       \\ \\__\\ \\__\\ \\_______\\ \\_______\\ \\__\\\\ \\__\\   \\ \\__\\\n" +
                "    \\|_______|\\|_______|\\|__|/       \\|_______|\\|_______|        \\|__|\\|__|\\|_______|\\|_______|\\|__| \\|__|    \\|__|\n" +
                "                                                                                                                   （v1.0.0）\n";

        System.out.println(banner);
    }
}
