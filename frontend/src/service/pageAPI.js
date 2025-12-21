import { request } from './http';

export const getPageData = async (pageId) => {
  return request(`/pages/${pageId}`, {
    method: 'GET',
  });
};

export const updatePageData = async (pageId, data) => {
  return request(`/pages/${pageId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
};
