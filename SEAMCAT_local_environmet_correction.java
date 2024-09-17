protected static LocalEnvCorrections localEnvCorrections(LocalEnvCorrections localEnvironmentCorrections,LinkResult linkResult,
           double floorHeight,double roomSize, double wiLoss, double floorLoss, double empiricalParam, double wiStdDev) {
 
       //LocalEnvCorrections result = new LocalEnvCorrections(0, localEnvironmentCorrections.rStdDev);
       LocalEnvironmentResult rxEnv = linkResult.rxAntenna().getLocalEnvironment();
       LocalEnvironmentResult txEnv = linkResult.txAntenna().getLocalEnvironment();
       // CAUTION : do NOT permute the order of the tests
       if ( rxEnv.getEnvironment() == Indoor && txEnv.getEnvironment() == Indoor) {
           if ( linkResult.isTxRxInSameBuilding() ) {
               // Transmitter and receiver are located in the same building
               // specific calculation : replaces standard calculation
               double rK;
 
               rK = Math.abs(
                       Math.floor(linkResult.txAntenna().getHeight()/ floorHeight) -
                       Math.floor(linkResult.rxAntenna().getHeight()/ floorHeight)
               );
 
               double d1 = linkResult.txAntenna().getHeight() - linkResult.rxAntenna().getHeight();
               double realDistance = Math.sqrt( (d1*d1) + (linkResult.getTxRxDistance()*linkResult.getTxRxDistance()) );
 
                localEnvironmentCorrections.rMedianLoss = -27.6
                       + 20.0
                       * Math.log10(1000 * realDistance)
                        +  20.0
                       * Math.log10(linkResult.getFrequency())
                       + Math.floor(1000 * linkResult.getTxRxDistance() / roomSize)
                       * wiLoss
                       + Math.pow(rK,
                       ((rK + 2.0) / (rK + 1.0) - empiricalParam))
                       * floorLoss;
               localEnvironmentCorrections.rStdDev = wiStdDev;
           } else {
               // Transmitter and receiver are located in different buildings
               // Calculation is similar to indoor-outdoor case with doubled
               // corrections
               localEnvironmentCorrections.rMedianLoss += rxEnv.getWallLoss() + txEnv.getWallLoss();
               localEnvironmentCorrections.rStdDev = Math
                       .sqrt(
                               localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev +
                                       ((txEnv.getWallLossStdDev() * txEnv.getWallLossStdDev()) +
                                               (rxEnv.getWallLossStdDev() * rxEnv.getWallLossStdDev())));
           }
       } else if (rxEnv.getEnvironment() == Indoor && txEnv.getEnvironment() == Outdoor) {
           localEnvironmentCorrections.rMedianLoss += rxEnv.getWallLoss();
           localEnvironmentCorrections.rStdDev = Math.sqrt(localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev +
                   rxEnv.getWallLossStdDev() * rxEnv.getWallLossStdDev());
       } else if (rxEnv.getEnvironment() == Outdoor && txEnv.getEnvironment() == Indoor) {
           localEnvironmentCorrections.rMedianLoss += txEnv.getWallLoss();
           localEnvironmentCorrections.rStdDev = Math.sqrt(localEnvironmentCorrections.rStdDev * localEnvironmentCorrections.rStdDev +
                   txEnv.getWallLossStdDev()*txEnv.getWallLossStdDev());
       }
       // outdoor outdoor => no correction
       return localEnvironmentCorrections;
       }