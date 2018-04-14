function progressive_sampling_plot(fileNum)
%% Prepares data
inputFile = sprintf('../data/ps_output/out%d.csv', fileNum);
csvMatrix = csvread(inputFile, 1, 0);
iteration = csvMatrix(:,1) + 1;
numSamples = csvMatrix(:,2);
fnIndex = csvMatrix(:,3) + 1;
objectiveMean = csvMatrix(:,4);
objectiveMin = csvMatrix(:,5);
objectiveMax = csvMatrix(:,6);
criterionIndex = csvMatrix(:,7) + 1;
criterionMean = csvMatrix(:,8);
criterionMin = csvMatrix(:,9);
criterionMax = csvMatrix(:,10);

numIteration = max(iteration);
numFn = max(fnIndex);
numCriteria = max(criterionIndex);

numSamplesI = NaN(numIteration,1);
objectiveMeanIF = NaN(numIteration, numFn);
objectiveMinIF = NaN(numIteration, numFn);
objectiveMaxIF = NaN(numIteration, numFn);
criterionMeanIFC = NaN(numIteration, numFn, numCriteria);
criterionMinIFC = NaN(numIteration, numFn, numCriteria);
criterionMaxIFC = NaN(numIteration, numFn, numCriteria);

fnNames = {'Lame MP3 Compression V1','Lame MP3 Compression V3',...
    'Lame MP3 Compression V5','Lame MP3 Compression V7',...
    'Lame MP3 Compression V9'};

criterionNames = {'PEAQ Objective Difference','Root Mean Squared Error',...
    'Compression Ratio','Compression Time',...
    'Decompression Time'};

for l=1:length(iteration)
    numSamplesI(iteration(l)) = numSamples(l);
    objectiveMeanIF(iteration(l),fnIndex(l)) = objectiveMean(l);
    objectiveMinIF(iteration(l),fnIndex(l)) = objectiveMin(l);
    objectiveMaxIF(iteration(l),fnIndex(l)) = objectiveMax(l);
    criterionMeanIFC(iteration(l),fnIndex(l),criterionIndex(l)) = criterionMean(l);
    criterionMinIFC(iteration(l),fnIndex(l),criterionIndex(l)) = criterionMin(l);
    criterionMaxIFC(iteration(l),fnIndex(l),criterionIndex(l)) = criterionMax(l);
end

%% Plot objective means
figure;
hold on;
for f=1:numFn
    errorbar(numSamplesI*(1+0.02*f),objectiveMeanIF(:,f),...
        objectiveMeanIF(:,f)-objectiveMinIF(:,f),...
        objectiveMaxIF(:,f)-objectiveMeanIF(:,f));
    
end
xlabel('Number of samples, $$S_i$$','interpreter','latex');
objY = sprintf('Objective value, $$V_{%d}(h(x))$$',fileNum);
ylabel(objY,'interpreter','latex');
xlim([80, 15000]);
objTitle = sprintf(...
        'Mean Objectives $V_{%d}$ and Confidence Intervals for Increasing Sample Size',...
        fileNum);
title(objTitle,'interpreter','latex');
legend(fnNames,'Location','southoutside','interpreter','latex');
set(gca,'XScale','log');
hold off;

%% Plot each criterion

for c=1:numCriteria
    figure;
    hold on;
    for f=1:numFn
        errorbar(numSamplesI*(1+0.02*f),criterionMeanIFC(:,f,c),...
            criterionMeanIFC(:,f,c)-criterionMinIFC(:,f,c),...
            criterionMaxIFC(:,f,c)-criterionMeanIFC(:,f,c));

    end
    xlabel('Number of samples');
    ylabel('Criterion Value');
    xlim([80, 15000]);
    critTitle = sprintf(...
        'Mean %s and Confidence Intervals for Increasing Sample Size',...
        criterionNames{c});
    title(critTitle);
    legend(fnNames,'Location','southoutside');
    set(gca,'XScale','log');
    hold off;
end

end

