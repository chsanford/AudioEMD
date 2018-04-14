function  peaq_vs_cr( )
csvTable = readtable('../data/cached_criteria/music.csv');

sample = csvTable.SAMPLE_INDEX + 1;
fnIndex = csvTable.FUNCTION_INDEX;
peaq = csvTable.PEAQ_OBJECTIVE_DIFFERENCE;
cr = csvTable.COMPRESSION_RATIO * 75 / 32;

fnNames = {'Lame MP3 Compression V1','Lame MP3 Compression V2',...
    'Lame MP3 Compression V3','Lame MP3 Compression V4',...
    'Lame MP3 Compression V5','Lame MP3 Compression V6',...
    'Lame MP3 Compression V7','Lame MP3 Compression V8',...
    'Lame MP3 Compression V9'};

hold on;
for f=1:9
    scatter(peaq(fnIndex == f), cr(fnIndex == f));
end
xlabel('PEAQ', 'interpreter', 'latex');
ylabel('Compression Ratio (Scaled up)', 'interpreter', 'latex');
legend(fnNames,'Location','southoutside','interpreter','latex');

end

