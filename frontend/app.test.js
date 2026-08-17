const request = require('supertest');
const axios = require('axios');
const MockAdapter = require('axios-mock-adapter');
const app = require('./app');

describe('GDS Frontend Route Tests', () => {
    let mockAxios;

    beforeEach(() => {
        // Intercept outgoing axios calls to Ktor backend
        mockAxios = new MockAdapter(axios);
    });

    afterEach(() => {
        mockAxios.restore();
    });

    describe('GET /', () => {
        it('renders the index page with URLs retrieved from Ktor', async () => {
            mockAxios.onGet('http://localhost:8080/urls').reply(200, [
                {
                    shortAlias: 'govuk',
                    fullUrl: 'https://www.gov.uk',
                    createdAt: Date.now()
                }
            ]);

            const response = await request(app).get('/');

            expect(response.status).toBe(200);
            expect(response.text).toContain('Shorten a URL');
            expect(response.text).toContain('https://www.gov.uk');
            expect(response.text).toContain('govuk');
        });

        it('renders fallback error banner if Ktor backend fails', async () => {
            mockAxios.onGet('http://localhost:8080/urls').reply(500);

            const response = await request(app).get('/');

            expect(response.status).toBe(200);

        });
    });

    describe('POST /shorten (Validation & Submission)', () => {
        it('returns validation error summary if fullUrl is empty', async () => {
            const response = await request(app)
                .post('/shorten')
                .type('form')
                .send({ fullUrl: '' });

            expect(response.status).toBe(200);
            expect(response.text).toContain('There is a problem');
            expect(response.text).toContain('Enter a full web address');
        });

        it('returns validation error summary if URL format is invalid', async () => {
            const response = await request(app)
                .post('/shorten')
                .type('form')
                .send({ fullUrl: 'not-a-valid-url' });

            expect(response.status).toBe(200);
            expect(response.text).toContain('There is a problem');
            expect(response.text).toContain('Enter a URL with a valid prefix');
        });

        it('successfully calls backend and renders success page for valid URL', async () => {
            mockAxios.onPost('http://localhost:8080/shorten').reply(201, {
                alias: 'abc1234'
            });

            const response = await request(app)
                .post('/shorten')
                .type('form')
                .send({ fullUrl: 'https://www.gov.uk' });

            expect(response.status).toBe(200);
            expect(response.text).toContain('abc1234');
            expect(response.text).toContain('https://www.gov.uk');
        });
    });

    describe('GET /:alias (Redirection)', () => {
        it('redirects user to target URL when backend returns 302/301', async () => {
            mockAxios.onGet('http://localhost:8080/my-link').reply(302, null, {
                location: 'https://www.gov.uk/check-mot-status'
            });

            const response = await request(app).get('/my-link');

            expect(response.status).toBe(302);
            expect(response.headers.location).toBe('https://www.gov.uk/check-mot-status');
        });

        it('renders 404 page when alias is not found on backend', async () => {
            mockAxios.onGet('http://localhost:8080/missing-link').reply(404);

            const response = await request(app).get('/missing-link');

            expect(response.status).toBe(404);
            expect(response.text).toContain('ShortUrl not found or expired');
        });
    });

    describe('POST /:alias (Deletion)', () => {
        it('triggers delete on Ktor and redirects back to root', async () => {
            mockAxios.onDelete('http://localhost:8080/del-link').reply(204);

            const response = await request(app).post('/del-link');

            expect(response.status).toBe(302);
            expect(response.headers.location).toBe('/');
        });
    });
});