const express = require('express');
const nunjucks = require('nunjucks');
const path = require('path');
const axios = require('axios');
//const {formatErrorMessage} = require("govuk-frontend/common/index.mjs");

const app = express();
const PORT = process.env.PORT || 3000;
const KTOR_API_URL = process.env.KTOR_API_URL || 'http://localhost:8080';

// Parse form submissions
app.use(express.urlencoded({ extended: true }));

// Serve compiled CSS, JS, and GOV.UK assets
app.use('/public', express.static(path.join(__dirname, 'public')));

// Configure Nunjucks view engine to read both local views and govuk-frontend macros
const appViews = [
    path.join(__dirname, 'views'),
    path.join(__dirname, 'node_modules/govuk-frontend/dist')
];

nunjucks.configure(appViews, {
    autoescape: true,
    express: app,
    noCache: true
});

// url validation function
function isValidUrl(string){
    try{
        const url = new URL(string.trim());
        return url.protocol === 'http:' || url.protocol === 'https:';
    }
    catch (_)
    {
        return false;
    }
}

app.set('view engine', 'njk');

// --- Routes ---

app.get('/', async (req, res) => {
    //res.render('index.njk');
    try{
        const response = await axios.get(`${KTOR_API_URL}/urls`);
        const urls = (response.data || []).map(item =>({
            ...item,
            //massage the date format into something more friendly
            createdAtFormatted: new Date(Number(item.createdAt)).toLocaleDateString('en-GB',{
            })
        }));

        res.render('index.njk', {urls})
    }
    catch (error){
        console.error('Failed to fetch from server', error.message);
        res.render('url-table.njk',{
            urls:[],
            errorMessage: 'Unable to retrieve URLs from server'
        });
    }
});

app.get('/:alias', async(req, res, next) =>{
    const {alias} = req.params

    if(alias === 'public' || alias === 'shorten') {
        return next();
    }

    //stop axios just processing the redirect message and pass it through to the client browser
    try {
        const response = await axios.get(`${KTOR_API_URL}/${alias}`,{
            maxRedirects:0,
            validateStatus: (status) => status >= 200 && status < 400
        });

        if(response.status >= 300 && response.status < 400){
            const redirectUrl = response.headers.location;

            //do browser redirect
            if(redirectUrl){
                return res.redirect(response.headers.location);
            }
        }
    }catch(error){
        console.error(`Error resolving alias ${alias}`, error.message);
        res.status(404).render('index.njk',{
            errorMessage: 'ShortUrl not found or expired'
        })
    }
})

app.post('/shorten', async (req, res) => {
    let { fullUrl } = req.body;
    const {customAlias} = req.body;

    if (!fullUrl || !fullUrl.trim()) {
        return res.render('index.njk', {
            errorMessage: 'Enter a full web address',
            fullUrl
        });
    }
    else if (!isValidUrl(fullUrl)){
        return res.render('index.njk',{
            errorMessage: 'Enter a URL with a valid prefix, http:// or https://',
                fullUrl
        });
    }

    try {
        // Post to Ktor Backend API
        const response = await axios.post(`${KTOR_API_URL}/shorten`, {
            fullUrl,
            customAlias: customAlias?.trim() || undefined
        });
        const shortAlias = response.data.alias;

        res.render('success.njk', { alias: shortAlias, fullUrl });
    } catch (error) {
        console.error('Ktor API Error:', error.message);
        res.render('index.njk', {
            errorMessage: 'Could not communicate with URL Shortener Service. Please try again.',
            fullUrl
        });
    }
});

app.post('/:alias', async(req,res) =>{
    const {alias} = req.params

    try{
        await axios.delete(`${KTOR_API_URL}/${alias}`);
        res.redirect('/');
    }
    catch(error){
        console.error(`Failed to delete ${alias}`, error.message);
        res.redirect('/')
    }
})


if(process.env.NODE_ENV !== 'test'){
    app.listen(PORT, () =>{
        console.log(`GDS Express Frontend running on http://localhost:${PORT}`);
    })
}

module.exports = app;