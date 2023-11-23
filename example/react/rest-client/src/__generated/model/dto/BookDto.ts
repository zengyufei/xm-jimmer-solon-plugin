import type {Gender} from '../enums';

export type BookDto = {
    'BookService/SIMPLE_FETCHER': {
        readonly id: number,
        readonly name: string,
        readonly edition: number
    },
    'BookService/COMPLEX_FETCHER': {
        readonly id: number,
        readonly createdTime: string,
        readonly modifiedTime: string,
        readonly name: string,
        readonly edition: number,
        readonly price: number,
        readonly store?: {
            readonly id: number,
            readonly createdTime: string,
            readonly modifiedTime: string,
            readonly name: string,
            readonly website?: string,
            readonly avgPrice: number
        },
        readonly authors: ReadonlyArray<{
            readonly id: number,
            readonly createdTime: string,
            readonly modifiedTime: string,
            readonly firstName: string,
            readonly lastName: string,
            readonly gender: Gender
        }>
    },
    'BookService/DEFAULT_FETCHER': {
        readonly id: number,
        readonly createdTime: string,
        readonly modifiedTime: string,
        readonly name: string,
        readonly edition: number,
        readonly price: number,
        readonly store?: {
            readonly id: number,
            readonly name: string
        },
        readonly authors: ReadonlyArray<{
            readonly id: number,
            readonly firstName: string,
            readonly lastName: string
        }>
    }
}
