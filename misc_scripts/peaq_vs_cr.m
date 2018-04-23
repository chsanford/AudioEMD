function  peaq_vs_cr( )
csvTable = readtable('../data/cached_criteria/music.csv');

sample = csvTable.SAMPLE_INDEX + 1;
fnIndex = csvTable.FUNCTION_INDEX + 1;
numFn = max(fnIndex);
peaq = csvTable.PEAQ_OBJECTIVE_DIFFERENCE;
cr = csvTable.COMPRESSION_RATIO * 75 / 32;
cr = min(cr, 1);

fnNames = {'Lame MP3 Compression V1','Lame MP3 Compression V2',...
    'Lame MP3 Compression V3','Lame MP3 Compression V4',...
    'Lame MP3 Compression V5','Lame MP3 Compression V6',...
    'Lame MP3 Compression V7','Lame MP3 Compression V8',...
    'Lame MP3 Compression V9','Lame MP3 Compression Fixed 320',...
    'Lame MP3 Compression Fixed 256', 'Lame MP3 Compression Fixed 128',...
    'Lame MP3 Compression Fixed 64'};

colors = distinguishable_colors(numFn);

hold on;
for f=1:numFn
    scatter(0,0,1,colors(f,:),'.');
end
for f=1:numFn
    s = scatter(peaq(fnIndex == f), cr(fnIndex == f), 10, colors(f,:), '.');
    s.MarkerFaceAlpha = 0.2;
    s.MarkerEdgeAlpha = 0.2;
end
xlabel('PEAQ', 'interpreter', 'latex');
ylabel('Compression Ratio (Scaled up)', 'interpreter', 'latex');
legend(fnNames,'Location','southoutside','interpreter','latex');
xlim([-0.1, 1.1]);
ylim([-0.1, 1.1]);

end

