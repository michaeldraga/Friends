const request = require('request');
const cheerio = require('cheerio');

function indexesOf(string, regex) {
    let match,
        indexes = {};

    regex = new RegExp(regex);

    while (match = regex.exec(string)) {
        if (!indexes[match[0]]) indexes[match[0]] = [];
        indexes[match[0]].push(match.index);
    }

    return indexes;
};

module.exports = async (lat, long) => {
    return new Promise((resolve, reject) => {
        const url = `https://www.google.com/maps/search/${lat},${long}`;
        require('request')(url, (err, res, body) => {
            // console.log(body);

            let lock = true;
            let index;
            for (let i = 0; i < body.length; i++) {
                if (body[i] === '\\n,null,\\"') {
                    if (lock) {
                        lock = false;
                        continue;
                    }
                    // console.log('yeet');
                    index = i;
                }
            }
            index = indexesOf(body, /(\\n,null,\\")/g)['\\n,null,\\"'][1];
            // console.log(index);
            let end;
            let fuck = indexesOf(body, /(\\",null,null,null,null)/g)['\\",null,null,null,null'];
            for (let fuc of fuck) {
                if (fuc > index) {
                    end = fuc;
                    break;
                }
            }
            // console.log(end);

            // console.log(body);

            console.log(body.slice(index + 10, end));
            resolve(body.slice(index + 10, end))
        })

        // console.log(body.toString().indexOf('\\n,null,\\"'))

        // $('.widget-pane-link').attr('jsan', (i, val) => {
        //     console.log(i);
        //     console.log(val);
        // })
    })
}